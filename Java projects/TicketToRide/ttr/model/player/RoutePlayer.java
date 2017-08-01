//Evaristo Koyama
//ek4ks

package ttr.model.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ttr.model.destinationCards.Destination;
import ttr.model.destinationCards.DestinationTicket;
import ttr.model.destinationCards.Route;
import ttr.model.destinationCards.Routes;
import ttr.model.trainCards.TrainCard;
import ttr.model.trainCards.TrainCardColor;

public class RoutePlayer extends Player {

	private ArrayList<Route> path;
	private DestinationTicket t;
	private Route current;
	private int amount;
	private boolean possible;

	public RoutePlayer() {
		super("Route Player");
		path = new ArrayList<Route>();
		amount = 0;
		possible = true;
	}

	public RoutePlayer(String name) {
		super(name);
		path = new ArrayList<Route>();
		amount = 0;
		possible = true;
	}

	private ArrayList<Route> getColorAndAmount(ArrayList<Route> a) {
		ArrayList<Route> ret = new ArrayList<Route>();
		amount = 0;
		for (Route r : a) {
			if (r.getOwner() != null && r.getOwner() != this) {
				return null;
			}
			else if (r.getOwner() == this) continue;

			if (r.getCost() > amount) {
				ret.clear();
				ret.add(r);
				amount = r.getCost();
			}
			else if (r.getCost() == amount) {
				ret.add(r);
			}
		}
		return ret;
	}

	private int getCost(ArrayList<Route> p) {
		int ret = 0;
		for (Route r : p) {
			if (r.getOwner() == null) {
				ret += r.getCost();
			}
		}
		return ret;
	}

	private int getNumRoutes(ArrayList<Route> p) {
		int ret = 0;
		for (Route r : p) {
			if (r.getOwner() == null) {
				ret++;
			}
		}
		return ret;
	}

	private double getPointsPerTrain(ArrayList<Route> p) {
		double points = 0;
		int trains = 0;
		for (Route r : p) {
			if (r.getOwner() == null) {
				points += r.getPoints();
				trains += r.getCost();
			}
		}
		return points/trains;
	}

	private ArrayList<Route> getPathWithLeastCities(Destination from, Destination to) {/* Open and Closed lists (breadth first search) */
		if(from == to) return null;

		HashMap<Destination, ArrayList<Route>> openList = new HashMap<Destination, ArrayList<Route>>();
		HashMap<Destination, ArrayList<Route>> closedList = new HashMap<Destination, ArrayList<Route>>();
		openList.put(from, new ArrayList<Route>());

		while(openList.size() > 0){

			/* Pop something off the open list, if destination then return true */
			Destination next = null;
			int minCost = 9999;
			double efficiency = 0;
			for(Destination key : openList.keySet()){
				ArrayList<Route> p = openList.get(key);
				int cost = getNumRoutes(p);
				double e  = getPointsPerTrain(p);
				if(cost < minCost) {
					next = key;
					minCost = cost;
					efficiency = e;
				}
				else if (cost == minCost && e > efficiency) {
					next = key;
					minCost = cost;
					efficiency = e;
				}
			}

			/* Take it off the open list and put on the closed list */
			ArrayList<Route> closed = openList.remove(next);
			closedList.put(next, closed);

			/* If this is the destination, then return!!!! */
			if(next == to) {
				return closed;
			}

			Routes routes = Routes.getInstance();
			/* Get all the neighbors of the next city that aren't on open or closed lists already */
			for(Destination neighbor : routes.getNeighbors(next)){
				if(closedList.containsKey(neighbor)) continue;

				/* get route between next and neighbor and see if better than neighbor's value */
				ArrayList<Route> routesToNeighbor = routes.getRoutes(next, neighbor);
				for(Route routeToNeighbor : routesToNeighbor){
					if ((routeToNeighbor.getOwner() != null) && (routeToNeighbor.getOwner() != this)) continue;

					ArrayList<Route> a = new ArrayList<Route>(closedList.get(next));
					a.add(routeToNeighbor);
					int newCost = getNumRoutes(a);

					if(openList.containsKey(neighbor)){	
						if(newCost < getNumRoutes(openList.get(neighbor))){
							openList.put(neighbor, a);
						}
					}
					else{
						openList.put(neighbor, a);
					}
				}
			}
		}
		return null;
	}


	private ArrayList<Route> getPathWithLeastCost(Destination from, Destination to) {
		if(from == to) return null;

		/* Open and Closed lists (breadth first search) */
		HashMap<Destination, ArrayList<Route>> openList = new HashMap<Destination, ArrayList<Route>>();
		HashMap<Destination, ArrayList<Route>> closedList = new HashMap<Destination, ArrayList<Route>>();
		openList.put(from, new ArrayList<Route>());

		while(openList.size() > 0){

			/* Pop something off the open list, if destination then return true */
			Destination next = null;
			int minCost = 9999;
			for(Destination key : openList.keySet()){
				ArrayList<Route> p = openList.get(key);
				int cost = getCost(p);
				if(cost <= minCost) {
					next = key;
					minCost = cost;
				}
			}

			/* Take it off the open list and put on the closed list */
			ArrayList<Route> closed = openList.remove(next);
			closedList.put(next, closed);

			/* If this is the destination, then return!!!! */
			if(next == to) {
				return closed;
			}

			Routes routes = Routes.getInstance();
			/* Get all the neighbors of the next city that aren't on open or closed lists already */
			for(Destination neighbor : routes.getNeighbors(next)){
				if(closedList.containsKey(neighbor)) continue;

				/* get route between next and neighbor and see if better than neighbor's value */
				ArrayList<Route> routesToNeighbor = routes.getRoutes(next, neighbor);
				for(Route routeToNeighbor : routesToNeighbor){
					if ((routeToNeighbor.getOwner() != null) && (routeToNeighbor.getOwner() != this)) continue;

					ArrayList<Route> a = new ArrayList<Route>(closedList.get(next));
					a.add(routeToNeighbor);
					int newCost = getCost(a);

					if(openList.containsKey(neighbor)){	
						if(newCost < getCost(openList.get(neighbor))){
							openList.put(neighbor, a);
						}
					}
					else{
						openList.put(neighbor, a);
					}
				}
			}
		}
		return null;
	}

	private int getIndex() {
		ArrayList<TrainCard> a = getFaceUpCards();
		if (current.getColor().equals(TrainCardColor.rainbow)) {
			TrainCardColor c = TrainCardColor.rainbow;
			int max = 0;
			for (TrainCardColor color : TrainCardColor.values()) {
				if (color.equals(TrainCardColor.rainbow)) continue;
				if (max < getNumTrainCardsByColor(color)) {
					c = color;
					max = getNumTrainCardsByColor(color);
				}
			}
			for (int i = 0; i < a.size(); i++) {
				if (a.get(i).getColor().equals(c)) {
					return i+1;
				}
			}
			for (int i = 0; i < a.size(); i++) {
				if (a.get(i).getColor().equals(TrainCardColor.rainbow)) {
					return i+1;
				}
			}
		}
		else {
			for (int i = 0; i < a.size(); i++) {
				if (a.get(i).getColor().equals(current.getColor())) {
					return i+1;
				}
			}
			for (int i = 0; i < a.size(); i++) {
				if (a.get(i).getColor().equals(TrainCardColor.rainbow)) {
					return i+1;
				}
			}
		}
		return 0;
	}

	private Route pickRoute(ArrayList<Route> a) {
		if (a.isEmpty()) return null;
		Route ret = a.get(0);
		int max = getNumTrainCardsByColor(ret.getColor());
		if (ret.getColor() != TrainCardColor.rainbow) {
			max += getNumTrainCardsByColor(TrainCardColor.rainbow);
		}
		for (Route r : a) {
			int num = getNumTrainCardsByColor(ret.getColor());
			if (ret.getColor() != TrainCardColor.rainbow) {
				num += getNumTrainCardsByColor(TrainCardColor.rainbow);
			} 
			if (num > max) {
				ret = r;
			}
		}
		return ret;
	}

	private boolean canClaim(Route r) {
		if (r.getCost() > getNumTrainPieces()) return false;

		if (r.getColor() == TrainCardColor.rainbow) {
			int rainbows = getNumTrainCardsByColor(r.getColor());
			for (TrainCardColor color : TrainCardColor.values()) {
				if (color == TrainCardColor.rainbow) continue;
				if (r.getCost() <= rainbows + getNumTrainCardsByColor(color)) return true;
			}
			return false;
		}
		else {
			return (r.getCost() <= getNumTrainCardsByColor(r.getColor()) + getNumTrainCardsByColor(TrainCardColor.rainbow));
		}
	}

	private void findAnotherPath() {	
		current = null;	
		t = getDestinationTickets().get(0);
		DestinationTicket other = null;
		if (getDestinationTickets().size() == 2) other = getDestinationTickets().get(1);
		if (other != null && other.getValue() < t.getValue()) {
			DestinationTicket temp = t;
			t = other;
			other = temp;	
		}
		if (getNumTrainPieces() > 35) {
			path = getPathWithLeastCities(t.getFrom(), t.getTo());
			if (path == null && other != null) {
				path = getPathWithLeastCities(other.getFrom(), other.getTo());
			}
		}
		else {
			path = getPathWithLeastCost(t.getFrom(), t.getTo());
			if (path == null && other != null) {
				path = getPathWithLeastCost(other.getFrom(), other.getTo());
			}
		}
		if (path != null) {
			ArrayList<Route> routes = getColorAndAmount(path);
			current = pickRoute(routes);
		}
		else {
			possible = false;
		}
		makeMove();

	}

	private void getRandomRoute(int limit) {
		Routes routes = Routes.getInstance();
		int best = 0;
		while (best == 0) {
			for (Route r : routes.getAllRoutes()) {
				if (routes.isRouteClaimed(r)) continue;
				int l = r.getCost();
				int num = 0;
				if (r.getColor() == TrainCardColor.rainbow) {
					l -= getNumTrainCardsByColor(TrainCardColor.rainbow);
					int max = 0;
					for (TrainCardColor color : TrainCardColor.values()) {
						if (color.equals(TrainCardColor.rainbow)) continue;
						num = getNumTrainCardsByColor(color);
						if (num > max) {
							num = max;
						}
					}
					l -= max;
				}
				else {
					l -= getNumTrainCardsByColor(r.getColor());
					l -= getNumTrainCardsByColor(TrainCardColor.rainbow);
				}
				int b = 2*r.getCost() - l;
				if (canClaim(r)) {
					b += r.getPoints();
				}
				if (r.getCost() > getNumTrainPieces()) {
					b = 0;
				}

				if (current == null && l < limit) {
					current = r;
					best = b;
				}
				else if (l < limit && b > best){
					current = r;
					best = b;
				}
			}
			limit += 1;
		}
		makeMove();
	}
	private boolean stolen() {
		ArrayList<Route> a = Routes.getInstance().getRoutes(current.getDest1(), current.getDest2());
		if (a.size() == 1 && a.get(0).getOwner() != null && a.get(0).getOwner() != this) {
			return true;
		}
		if (a.size() == 2 && ((a.get(0).getOwner() != null && a.get(0).getOwner() != this) ||
				(a.get(1).getOwner() != null && a.get(1).getOwner() != this))){
			return true;
		}
		return false;
	}

	@Override
	public void makeMove() {
		if (current != null && current.getOwner() == this) {
			path.remove(current);
			current = null;
		}

		if (path.isEmpty() && getNumTrainPieces() == 45) {
			findAnotherPath();
		}

		if (current == null && (path == null || path.isEmpty()) && (getDestinationTickets().isEmpty() || !possible)) {
			getRandomRoute(3);
		}
		else if (current == null && path == null) {
			findAnotherPath();
		}

		else if (current == null && path.isEmpty()) {
			findAnotherPath();
		}
		else if (current == null && !path.isEmpty()) {
			ArrayList<Route> routes = getColorAndAmount(path);
			if (routes == null || routes.isEmpty()) {
				if (getDestinationTickets().isEmpty()) getRandomRoute(3);
				else findAnotherPath();			
			}
			else {
				current = pickRoute(routes);
				makeMove();
			}
		}
		else if (stolen()) {
			if (getDestinationTickets().isEmpty() || !possible) getRandomRoute(3);
			else findAnotherPath();
		}
		else if (canClaim(current)) {
			if (current.getColor() == TrainCardColor.rainbow) {
				TrainCardColor c = TrainCardColor.rainbow;
				int max = 0;
				for (TrainCardColor color : TrainCardColor.values()) {
					if (color == TrainCardColor.rainbow) continue;
					if (max < getNumTrainCardsByColor(color)) {
						c = color;
						max = getNumTrainCardsByColor(color);
					}
				}
				claimRoute(current, c);
			}
			else {
				claimRoute(current, current.getColor());
			}
		}
		else {
			drawTrainCard(getIndex());
		}
	}
}
