package ttr.model.player;

import java.util.ArrayList;
import java.util.HashMap;

import ttr.model.destinationCards.Route;
import ttr.model.destinationCards.Routes;
import ttr.model.trainCards.TrainCard;
import ttr.model.trainCards.TrainCardColor;

public class TrainPlayer extends Player {
	
	private HashMap<Integer, ArrayList<Route>> costs;
	private TrainCardColor current;
	private int amount;

	public TrainPlayer() {
		super("Train Player");
		initializeCosts();
	}
	
	public TrainPlayer(String name) {
		super(name);
		initializeCosts();
	}

	private void initializeCosts() {
		costs = new HashMap<Integer, ArrayList<Route>>();
		ArrayList<Route> routes = Routes.getInstance().getAllRoutes();
		for (Route r : routes) {
			if (!costs.containsKey(r.getCost())) {
				ArrayList<Route> a = new ArrayList<Route>();
				a.add(r);
				costs.put(r.getCost(), a);
			}
			else {
				ArrayList<Route> a = costs.get(r.getCost());
				a.add(r);
				costs.put(r.getCost(), a);
			}
		}	
	}
	
	private void updateCosts() {
		ArrayList<Route> routes = Routes.getInstance().getAllRoutes();
		for (Route r : routes) {
			if (Routes.getInstance().isRouteClaimed(r) && costs.get(r.getCost()).contains(r)) {
				ArrayList<Route> a = costs.get(r.getCost());
				a.remove(r);
				costs.put(r.getCost(), a);
			}
		}	
	}
	
	private void getColorAndAmount() {
		current = null;
		amount = 0;
		for (TrainCardColor c : TrainCardColor.values()) {
			int a = 0;
			for (TrainCard tc : getHand()) {
				if (c.equals(tc.getColor()) || tc.getColor().equals(TrainCardColor.rainbow)) {
					a++;
				}
			}
			if (a > amount) {
				amount = a;
				current = c;
			}
		}
	}
	private Route getRoute() {
		if (current == null || amount == 0) {
			return null;
		}
		for (int i = 6; i > 0; i--) {
			if (i > getNumTrainPieces()) continue;
			ArrayList<Route> a = costs.get(i);
			for (Route r : a) {
				if ((r.getColor() == current || r.getColor() == TrainCardColor.rainbow) && r.getCost() > amount) {
					return null;
				}
				if ((r.getColor() == current || r.getColor() == TrainCardColor.rainbow) && r.getCost() == amount) {
					return r;
				}
			}
		}
		return null;
	}
	
	private int getIndex() {
		ArrayList<TrainCard> a = getFaceUpCards();
		if (current.equals(TrainCardColor.rainbow)) {
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
				if (a.get(i).getColor().equals(current)) {
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


	@Override
	public void makeMove() {
		updateCosts();
		getColorAndAmount();
		Route r = getRoute();
		if (r != null) {
			claimRoute(r, current);
		}
		else {
			drawTrainCard(getIndex());
		}

	}

	
}
