import java.awt.Point;
import java.util.HashMap;
/*
 * Evaristo Koyama
 * ek4ks
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import world.Robot;
import world.World;

public class CertainRobot extends Robot {

	protected Point end;
	protected int endX;
	protected int endY;

	public static void main(String[] args) {
		try{
			World myWorld = new World("input1", false);

			CertainRobot r = new CertainRobot();
			r.addToWorld(myWorld);
			r.travelToDestination();
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public void addToWorld(World world) {
		super.addToWorld(world);
		end = world.getEndPos();
		endX = world.numRows();
		endY = world.numCols();
	}

	public void travelToDestination() {
		PriorityQueue<Path> pq = new PriorityQueue<Path>(1, new PathComparator(end));
		HashMap<Point, Integer> paths = new HashMap<Point, Integer>();
		Path path = new Path();
		path.add(getPosition());
		pq.add(path);
		while (!pq.isEmpty()) {
			path = pq.poll();
			LinkedList<Point> next = getNextPoints(path);
			for (Point p : next) {
				Path temp = new Path(path);
				temp.add(p);
				if (p.equals(end)) {
					moveToDestination(temp);
				}
				if (!paths.containsKey(p) || (temp.size() < paths.get(p) && paths.get(p) > 0)) {
					String s = "";
					if (!paths.containsKey(p)) {
						s = pingMap(p);
					}
					else if (paths.get(p) < 0) {
						s = "X";
					}
					if ("OF".contains(s)) {
						pq.add(temp);
						paths.put(p, temp.size());
					}
					else {
						paths.put(p, -1);
					}
				}
			}
		}
		System.out.println("Cannot reach destination");
	}

	public void moveToDestination(Path p) {
		if (p.getList().getFirst().equals(getPosition())) {
			p.remove();
		}
		for (Point point : p.getList()) {
			super.move(point);
		}
	}
	public LinkedList<Point> getNextPoints(Path p) {
		LinkedList<Point> next = new LinkedList<Point>();
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i != 0 || j != 0) {
					Point last = new Point(p.getLast());
					last.translate(i, j);
					if (!p.contains(last) && (int)(last.getX()) >= 0 && (int)(last.getY()) >= 0 
							&& (int)(last.getX()) < endX && (int)(last.getY()) < endY) {
						next.add(last);
					}
				}
			}
		}
		return next;
	}
	public boolean checkPath(HashMap<Point, Boolean> map, Path path) {
		boolean fresh = false;
		for (Point p : path.getList()) {
			if (!map.containsKey(p)) {
				fresh = true;
				break;
			}
		}
		return fresh;
	}
}

class Path {
	private LinkedList<Point> path;
	public Path() {
		path = new LinkedList<Point>();
	}

	public Path(Path pa) {
		path = new LinkedList<Point>();
		for (Point p : pa.getList()) {
			path.add((Point) p.clone());
		}
	}

	public LinkedList<Point> getList() {
		return path;
	}

	public void add(Point p) {
		path.add(p); 
	}

	public void remove() {
		path.remove();
	}

	public boolean contains(Point p) {
		return path.contains(p);
	}

	public Point getLast() {
		return path.getLast();
	}

	public int size() {
		return path.size();
	}

	public String toString() {
		String s = "";
		for (Point p : path) {
			s += "(" + Integer.toString((int)p.getX()) + ", "  + Integer.toString((int)p.getY()) + ") ";
		}
		return s;
	}

	public int hashCode() {
		int code = 1;
		for (Point p : path) {
			code = code * p.hashCode();
		}
		return code;
	}

	public boolean equals(Object o) {
		if (o.getClass() == this.getClass()) {
			LinkedList<Point> path2 = ((Path)(o)).getList();
			if (path.size() != path2.size()) {
				return false;
			}
			Iterator<Point> it = path.iterator();
			Iterator<Point> it2 = path2.iterator();
			while (it.hasNext()) {
				if(!it.next().equals(it2.next())) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
}	
