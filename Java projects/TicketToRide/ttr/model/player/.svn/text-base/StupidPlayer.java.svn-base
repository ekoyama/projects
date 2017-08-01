package ttr.model.player;

/**
 * A very stupid player that simply draws train cards only. Shown as an example of implemented a player.
 * */
public class StupidPlayer extends Player{

	/**
	 * Need to have this constructor so the player has a name, you can use no parameters and pass the name of your player
	 * to the super constructor, or just take in the name as a parameter. Both options are shown here.
	 * */
	public StupidPlayer(String name) {
		super(name);
	}
	public StupidPlayer(){
		super("Stupid Player");
	}
	
	/**
	 * MUST override the makeMove() method and implement it.
	 * */
	@Override
	public void makeMove(){
		/* Always draw train cards (0 means we are drawing from the pile, not from the face-up cards) */
		super.drawTrainCard(0);
		
	}

}
