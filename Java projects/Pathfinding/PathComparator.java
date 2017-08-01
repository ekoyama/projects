/*
 * Evaristo Koyama
 * ek4ks
 */

import java.awt.Point;
import java.util.Comparator;

public class PathComparator implements Comparator<Path> {
	
	private Point end;
	
	public PathComparator(Point e) {
		end = e;
	}

	public int compare(Path path1, Path path2) {
		int p1 = path1.size() + (int)(Math.max(Math.abs(path1.getLast().getX()-end.getX()), 
				Math.abs(path1.getLast().getY()-end.getY())));
		int p2 = path2.size() + (int)(Math.max(Math.abs(path2.getLast().getX()-end.getX()), 
				Math.abs(path2.getLast().getY()-end.getY())));
		return p1-p2;
	}

}
