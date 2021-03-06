package asmcup.runtime;

import java.util.*;

public class World {
	protected final ArrayList<Robot> robots;
	protected final HashMap<Integer, Cell> cells;
	protected int seed;
	protected int frame;
	
	public World() {
		this(new Random().nextInt());
	}
	
	public World(int seed) {
		this.robots = new ArrayList<>();
		this.cells = new HashMap<>();
		this.seed = seed;
		this.frame = 0;
	}
	
	public Iterable<Robot> getRobots() {
		return robots;
	}
	
	public int getSeed() {
		return seed;
	}
	
	public void addRobot(Robot robot) {
		robots.add(robot);
	}
	
	public Cell getCell(int cellX, int cellY) {
		int key = Cell.key(cellX, cellY);
		Cell cell = cells.get(key);
		
		if (cell == null) {
			cell = new Cell(this, cellX, cellY);
			cells.put(key, cell);
		}
		
		return cell;
	}
	
	public Cell getCellXY(int x, int y) {
		return getCell(x / CELL_SIZE, y / CELL_SIZE);
	}
	
	public Cell getCellXY(float x, float y) {
		return getCellXY((int)x, (int)y);
	}
	
	public int getTile(int column, int row) {
		int cellX = column / TILES_PER_CELL;
		int cellY = row / TILES_PER_CELL;
		Cell cell = getCell(cellX, cellY);
		return cell.getTile(column - cellX * TILES_PER_CELL,
		                    row - cellY * TILES_PER_CELL);
	}
	
	public int getTileXY(float x, float y) {
		return getTile((int)(x / TILE_SIZE), (int)(y / TILE_SIZE));
	}
	
	public boolean isSolid(float x, float y) {
		int left = (int)(x / CELL_SIZE) * CELL_SIZE;
		int top = (int)(y / CELL_SIZE) * CELL_SIZE;
		return getCellXY(x, y).isSolidXY(x - left, y - top);
	}
	
	public boolean isSolid(float x, float y, float r) {
		return isSolid(x, y) || isSolid(x - r, y - r) || isSolid(x + r, y + r) || isSolid(x - r, y + r)
				|| isSolid(x + r, y - r);
	}
	
	public int getHazard(float x, float y) {
		int tile = getTileXY(x, y);
		
		if ((tile & 0b111) != Cell.TILE_HAZARD) {
			return -1;
		}
		
		return tile >> 3;
	}
	
	public float ray(float x, float y, float theta) {
		float cos = (float)Math.cos(theta);
		float sin = (float)Math.sin(theta);
		
		for (int i = 0; i < RAY_STEPS; i++) {
			float tx = x + (cos * i * RAY_INTERVAL);
			float ty = y + (sin * i * RAY_INTERVAL);
			
			if (isSolid(tx, ty)) {
				return i * RAY_INTERVAL;
			}
		}
		
		return RAY_STEPS * RAY_INTERVAL;
	}
	
	public void tick() {
		for (Robot robot : robots) {
			robot.tick(this);
			tickItems(robot);
			tickHazards(robot);
		}
		
		frame++;
	}
	
	protected void tickHazards(Robot robot) {
		switch (getHazard(robot.getX(), robot.getY())) {
		case 0:
			robot.damage(Robot.BATTERY_MAX / 500);
			break;
		case 1:
			robot.damage(Robot.BATTERY_MAX / 250);
			break;
		case 2:
			robot.damage(Robot.BATTERY_MAX / 100);
			break;
		case 3:
			robot.kill();
			break;
		}
	}
	
	protected void tickItems(Robot robot) {
		Cell cell = getCellXY(robot.getX(), robot.getY());
		Item item = cell.getItem(robot);
		
		if (item == null) {
			return;
		}
		
		item.collect(robot);
		cell.removeItem(item);
	}

	public void mark(Robot robot, int offset, int value) {
		// TODO read data from tile
	}

	public int markRead(Robot robot, int offset) {
		// TODO write data to tile
		return 0;
	}
	
	public void send(Robot robot, float frequency, int data) {
		// TODO send data on radio
	}
	
	public int recv(Robot robot, float frequency) {
		// TODO read data on radio
		return 0;
	}
	
	public static final int RAY_INTERVAL = 4;
	public static final int RAY_STEPS = 64;
	
	public static final int TILE_SIZE = 32;
	public static final int TILE_HALF = TILE_SIZE / 2;
	public static final int TILES_PER_CELL = 16;
	public static final int CELL_SIZE = TILES_PER_CELL * TILE_SIZE;
	public static final int CELL_COUNT = 0xFF;
	public static final int SIZE = TILE_SIZE * TILES_PER_CELL * CELL_COUNT;
}
