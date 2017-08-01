package ttr.model.player;

import java.util.ArrayList;
import java.util.HashMap;
import ttr.model.destinationCards.Destination;
import ttr.model.destinationCards.DestinationTicket;
import ttr.model.destinationCards.Route;
import ttr.model.destinationCards.Routes;
import ttr.model.trainCards.TrainCard;
import ttr.model.trainCards.TrainCardColor;

public class ek4ks extends Player {

	private ArrayList<Route> path;
	private DestinationTicket t;
	private HashMap<TrainCardColor, Route> focus;
	private int amount;
	private boolean possible;

	public ek4ks() {
		super("AI");
		path = new ArrayList<Route>();
		amount = 0;
		focus = new HashMap<TrainCardColor, Route>();
		possible = true;
	}

	public ek4ks(String name) {
		super(name);
		path = new ArrayList<Route>();
		amount = 0;
		focus = new HashMap<TrainCardColor, Route>();
		possible = true;
	}

	private ArrayList<Route> getColorAndAmount(ArrayList<Route> a) {
		ArrayList<Route> ret = new ArrayList<Route>();
		amount = 0;
		for (Route r : a) {
			if (r.getOwner() != null && r.getOwner() != this) {
				return null;
			}
			else if (Routes.getInstance().isRouteClaimed(r)) continue;

			ArrayList<Route> a2 = Routes.getInstance().getRoutes(r.getDest1(), r.getDest2());
			for (Route r2 : a2) {
				if (r2.getCost() > amount) {
					amount = r2.getCost();
				}
				ret.add(r2);
			}
		}
		return ret;
	}

	private int getCost(ArrayList<Route> p) {
		HashMap<TrainCardColor, Integer> cards = new HashMap<TrainCardColor, Integer>();
		for (TrainCardColor color : TrainCardColor.values()) {
			cards.put(color, getNumTrainCardsByColor(color));
		}
		int ret = 0;
		for (Route r : p) {
			if (r.getColor().equals(TrainCardColor.rainbow)) continue;
			if (r.getOwner() == null) {
				int add = r.getCost();
				if (cards.get(r.getColor()) > add) {
					add = 0;
					cards.put(r.getColor(), cards.get(r.getColor())-add);
				}
				else {
					add -= cards.get(r.getColor());
					cards.put(r.getColor(), 0);
				}
				ret += add;
			}
		}
		for (Route r : p) {
			if (!r.getColor().equals(TrainCardColor.rainbow)) continue;
			if (r.getOwner() == null) {
				int add = r.getCost();
				for (TrainCardColor color : cards.keySet()) {
					int num = cards.get(color);
					if (num > 0) {
						if (num > add) {
							add = 0;
							cards.put(color, num-add);
						}
						else {
							add -= num;
							cards.put(color, 0);
						}
						break;
					}
				}
				ret += add;
			}
		}
		return ret-cards.get(TrainCardColor.rainbow);
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
		if (p == null) return 0;
		double points = 0;
		int trains = 0;
		for (Route r : p) {
			if (r.getOwner() == null) {
				points += r.getPoints();
				trains += r.getCost();
			}
			if (r.getOwner() != null && r.getOwner() != this) return 0;
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
		HashMap<Integer, ArrayList<TrainCardColor>> cards = new HashMap<Integer, ArrayList<TrainCardColor>>();
		for (TrainCardColor color: focus.keySet()) {
			Route route = focus.get(color);
			int num = getNumTrainCardsByColor(color);
			if (route.getColor().equals(TrainCardColor.rainbow)) {
				continue;
			}
			else if (cards.containsKey(num)){
				ArrayList<TrainCardColor> c = cards.get(num);
				c.add(color);
				cards.put(num, c);
			}
			else {
				ArrayList<TrainCardColor> c = new ArrayList<TrainCardColor>();
				c.add(color);
				cards.put(num, c);
			}
		}
		for (int i = 12; i >= 0; i--) {
			if (!cards.containsKey(i)) continue;
			ArrayList<TrainCardColor> c = cards.get(i);
			for (TrainCardColor color : c) {
				for (int j = 0; j < a.size(); j++) {
					if (a.get(j).equals(color)) return j+1;
				}
			}
		}
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).equals(TrainCardColor.rainbow)) return i+1;
		}
		return 0;
	}

	private HashMap<TrainCardColor, Route> pickRoute(ArrayList<Route> a) {
		HashMap<TrainCardColor, Route> ret = new HashMap<TrainCardColor, Route>();	
		for (TrainCardColor color : TrainCardColor.values()) {
			if (getNumTrainCardsByColor(color) > 3 || color.equals(TrainCardColor.rainbow)) {
				Route r = null;
				int max = 0;
				for (Route route : Routes.getInstance().getAllRoutes()) {
					if (Routes.getInstance().isRouteClaimed(route)) continue;
					if (a.contains(route)) {
						r = route;
						break;
					}

					if ((route.getColor().equals(color) || route.getColor().equals(TrainCardColor.rainbow)) && route.getCost() > max) {
						r = route;
						max = route.getCost();
					}
				}
				if (r != null && !a.contains(ret.get(r.getColor()))) {
					ret.put(r.getColor(), r);
				}
			}
		}


		for (Route route : a) {
			TrainCardColor color = route.getColor();
			if (ret.containsKey(color)) {
				if (ret.get(color).getCost() < route.getCost()) ret.put(color, route);
			}
			else {
				ret.put(color, route);
			}
		}
		return ret;
	}

	private boolean canClaim(Route r) {
		if (r.getCost() > getNumTrainPieces()) return false;
		ArrayList<Route> a = Routes.getInstance().getRoutes(r.getDest1(), r.getDest2());
		for (Route route : a) {
			if (route.getOwner() != null) return false;
		}

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
		focus.clear();	
		DestinationTicket dt1 = null;
		DestinationTicket dt2 = null;
		double max1 = 0;
		double max2 = 0;
		for (DestinationTicket dt : getDestinationTickets()) {
			double val = 0;
			if (dt.getValue() < 15 && getNumTrainPieces() > 30)  val = getPointsPerTrain(getPathWithLeastCities(dt.getFrom(), dt.getTo()));
			else val = getPointsPerTrain(getPathWithLeastCost(dt.getFrom(), dt.getTo()));		
			if (val > max1) {
				if (dt1 != null) {
					dt2 = dt1;
					max2 = max1;
				}
				dt1 = dt;
				max1 = val;
			}
			if (val < max1 && val > max2) {
				dt2 = dt;
				max2 = dt.getValue();
			}
		}
		t = dt1;
		DestinationTicket other = dt2;
		if (other != null && other.getValue() < t.getValue()) {
			DestinationTicket temp = t;
			t = other;
			other = temp;	
		}
		if (t.getValue() < 15 && getNumTrainPieces() > 30) {
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
			focus = pickRoute(routes);
			makeMove();
		}
		else {
			possible = false;
		}
		makeMove();
	}

	private void getRandomRoute() {
		int limit = 0;
		if (getNumTrainPieces() > 22) {
			limit = 4;
		}
		else {
			limit = 3;
		}
		Routes routes = Routes.getInstance();
		do {
			for (int i = 1; i <= 6; i++) {
				if (i > getNumTrainPieces()) break;
				for (Route route : routes.getAllRoutes()) {
					if (routes.isRouteClaimed(route)) continue;
					if (route.getCost() == i) {
						if (!focus.containsKey(route.getColor())) {
							focus.put(route.getColor(), route);
						}
						else {
							Route r = focus.get(route.getColor());
							int l = route.getCost(); 
							int num = 0;
							if (route.getColor() == TrainCardColor.rainbow) {
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
								l -= getNumTrainCardsByColor(route.getColor());
								l -= getNumTrainCardsByColor(TrainCardColor.rainbow);
							}
							if (r.getCost() < route.getCost() && l < limit) {
								focus.put(route.getColor(), route);
							}
						}
					}
				}
			}
			limit++;
		} while (focus.isEmpty());
	}

	private boolean stolen() {
		for (TrainCardColor color : focus.keySet()) {
			Route route = focus.get(color);
			ArrayList<Route> a = Routes.getInstance().getRoutes(route.getDest1(), route.getDest2());
			if (a.size() == 1 && a.get(0).getOwner() != null && a.get(0).getOwner() != this) {
				return true;
			}
			if (a.size() == 2 && ((a.get(0).getOwner() != null && a.get(0).getOwner() != this) ||
					(a.get(1).getOwner() != null && a.get(1).getOwner() != this))) {
				return true;
			}
		}
		return false;
	}

	private void recalculateRoute() {
		ArrayList<Route> routes = getColorAndAmount(path);
		if (routes == null || routes.isEmpty()) {
			if (getDestinationTickets().isEmpty()) getRandomRoute();
			else findAnotherPath();			
		}
		else {
			focus = pickRoute(routes);
			makeMove();
		}
	}

	private Route findClaimableRoute() {
		Route r = null;
		boolean b = false;
		for (TrainCardColor color : focus.keySet()) {
			Route route = focus.get(color);
			if (canClaim(route)) {
				if (b && path != null && path.contains(route) && route.getCost() > r.getCost()) {
					r = route;
				}
				else if (!b && path != null && path.contains(route)) {
					b = true;
					r = route;
				}
				else if (!b && r == null) {
					r = route;
				}
				else if (!b && route.getCost() > r.getCost()) {
					r = route;
				}
			}
		}
		return r;
	}

	@Override
	public void makeMove() {
		if (!focus.isEmpty()) {
			ArrayList<Route> remove = new ArrayList<Route>();
			for (Route route : path) {
				ArrayList<Route> a = Routes.getInstance().getRoutes(route.getDest1(), route.getDest2());
				if (a.size() == 2 &&  (a.get(0).getOwner() == this || a.get(1).getOwner() == this)) {
					if (focus.containsKey(a.get(0).getColor()) && focus.get(a.get(0).getColor()).equals(route)) focus.remove(a.get(0).getColor());
					if (focus.containsKey(a.get(1).getColor()) && focus.get(a.get(1).getColor()).equals(route)) focus.remove(a.get(1).getColor());
				}
				if (a.size() == 1 && a.get(0).getOwner() == this)  focus.remove(a.get(0).getColor());
				if (route.getOwner() == this) remove.add(route);
			}
			for (Route route : remove) {
				if (path.contains(route)) path.remove(route);
			}
		}
		System.out.println(focus.entrySet());
		Route claim = findClaimableRoute(); 
		if (claim != null) {
			ArrayList<Route> a = Routes.getInstance().getRoutes(claim.getDest1(), claim.getDest2());
			if (a.size() == 2 &&  (a.get(0).getOwner() == this || a.get(1).getOwner() == this)) {
				if (focus.containsKey(a.get(0).getColor()) && focus.get(a.get(0).getColor()).equals(claim)) focus.remove(a.get(0).getColor());
				if (focus.containsKey(a.get(1).getColor()) && focus.get(a.get(1).getColor()).equals(claim)) focus.remove(a.get(1).getColor());
			}
			if (a.size() == 1 && a.get(0).getOwner() == this)  focus.remove(a.get(0).getColor());
			if (claim.getColor() == TrainCardColor.rainbow) {
				TrainCardColor c = TrainCardColor.rainbow;
				int max = 0;
				for (TrainCardColor color : TrainCardColor.values()) {
					if (color == TrainCardColor.rainbow) continue;
					if (max < getNumTrainCardsByColor(color)) {
						c = color;
						max = getNumTrainCardsByColor(color);
					}
				}
				claimRoute(claim, c);
			}
			else {
				claimRoute(claim, claim.getColor());
			}
		}



		if (path.isEmpty() && getNumTrainPieces() == 45 && getDestinationTickets().size() == 2 && 
				(getDestinationTickets().get(0).getValue() + getDestinationTickets().get(1).getValue() < 15)) {
			drawDestinationTickets();
		}

		if (path.isEmpty() && getNumTrainPieces() == 45) {
			findAnotherPath();
		}

		if (focus.isEmpty() && (path == null || path.isEmpty()) && (getDestinationTickets().isEmpty() || !possible)) {
			getRandomRoute();
		}
		else if (focus.isEmpty() && path == null) {
			findAnotherPath();
		}

		else if (focus.isEmpty() && path.isEmpty()) {
			findAnotherPath();
		}
		else if (focus.isEmpty() && !path.isEmpty()) {
			recalculateRoute();
		}
		else if (stolen()) {
			if (getDestinationTickets().isEmpty()) getRandomRoute();
			else recalculateRoute();
		}

		else {
			drawTrainCard(getIndex());
		}
	}
}
