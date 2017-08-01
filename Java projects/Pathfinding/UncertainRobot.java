/*
 * Evaristo Koyama
 * ek4ks
 */


import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import world.World;

public class UncertainRobot extends CertainRobot {

	public static void main(String[] args) {
		try{
			World myWorld = new World("input4", true);

			UncertainRobot r = new UncertainRobot();
			r.addToWorld(myWorld);
			r.travelToDestination();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public int moveToDestination(HashMap<Point, Boolean> m, Path p) {
		if (p.getList().getFirst().equals(getPosition())) {
			p.remove();
		}
		int i = 0;
		for (Point point : p.getList()) {
			Point current = getPosition();
			Point next = super.move(point);
			if (current.equals(next)) {
				m.put(point, false);
				break;
			}
			else {
				m.put(point, true);
				i++;
			}
		}
		return i;
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
	
	@Override
	public void travelToDestination() {
		HashMap<Point, Boolean> map = new HashMap<Point, Boolean>();
		map.put(getPosition(), true);
		map.put(end, true);
		int length = 5;
		while (!getPosition().equals(end)) {
			PriorityQueue<Path> pq = new PriorityQueue<Path>(1, new PathComparator(end));
			HashMap<Point, Integer> paths = new HashMap<Point, Integer>();
			Path path = new Path();
			path.add(getPosition());
			pq.add(path);
			while (!pq.isEmpty()) { 
				path = pq.poll();
				if (path.size() > length && checkPath(map, path)) {
					break;
				}
				else if (path.size() > length && !checkPath(map, path)) {
					length = path.size()+1;
				}
				LinkedList<Point> next = getNextPoints(path);
				for (Point p : next) {
					Path temp = new Path(path);
					temp.add(p);
					if (p.equals(end)) {
						path = new Path(temp);
						break;
					}
					if (!paths.containsKey(p) || (temp.size() < paths.get(p) && paths.get(p) > 0)) {
						String s = "";
						if (map.containsKey(p)) {
							if (!map.get(p)) {
								s = "X";
							}
						}
						else if (!paths.containsKey(p)) {
							s = pingMap(p);
						}
						else if (paths.get(p) < 0) {
							s = "X";
						}
						if ("SOF".contains(s)) {
							pq.add(temp);
							paths.put(p, temp.size());
						}
						else {
							paths.put(p, -1);
						}
					}
				}
				if (path.getLast().equals(end)) {
					break;
				}
			}
			if (checkPath(map, path) && path.size() > 0) {
				int len = moveToDestination(map, path);
				length = Math.max(5, len+1);
			}
			else {
				length++;
			}
		}
	}
}