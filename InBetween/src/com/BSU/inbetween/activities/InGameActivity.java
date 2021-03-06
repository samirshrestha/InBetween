package com.BSU.inbetween.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.*;

import com.BSU.inbetween.*;
import com.edu.BSU.inbetween.common.*;

public class InGameActivity extends Activity {
	
	boolean isRoundOver = false;
	boolean isGameOver = false;
	GameSession session;
	boolean roundOver;
	boolean displayAIOne = false;
	boolean displayAITwo = false;
	boolean displayAIThree = false;
	boolean displayAIFour = false;
	boolean displayAIFive = false;
	private boolean betting;
	private int betAmount = 1;
	int aiPlayers;
	int startingMoney;
	int defaultPotSize;
	int anteAmount;
	private String fileName = StringValues.SavedSettingsSharedValues.toString();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreenLandscape();
		loadSavedSettings();
		initializeSession();
		prepareButtonsAndListeners();
		prepareGameValues(); 
	}
	
	private void setFullScreenLandscape() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_in_game);
		setRequestedOrientation(0);// 0 means landscape or horizontal
	}

	private void loadSavedSettings() {
		SharedValues values = null;
		try {
			values = SharedValues.getInstance(this.getSharedPreferences(fileName, 4),this.getResources());
		} catch (Exception e) {
			Log.i("Shared Preferences IO ERROR:", e.getMessage());
		} finally {
			aiPlayers = values.getAmountOfPlayers();
			startingMoney = values.getStartingMoney();
			defaultPotSize = values.getPotSize();
			anteAmount = values.getAnteAmount();
		}
	}

	private void initializeSession() {
		session = new GameSession(aiPlayers, startingMoney, defaultPotSize, anteAmount, super.getAssets());
		session.populatePlayerList();
	}
	
	private void prepareButtonsAndListeners() {
		Button betButton = (Button) findViewById(R.id.bet);
		Button passButton = (Button) findViewById(R.id.pass);
		Button pauseButton = (Button) findViewById(R.id.pause);
		Button nextRoundButton = (Button) findViewById(R.id.nextRound);
		betButton.setOnClickListener(betClick);
		passButton.setOnClickListener(passClick);
		pauseButton.setOnClickListener(pauseClick);
		nextRoundButton.setOnClickListener(nextRoundClick);
		nextRoundButton.setEnabled(false);
	}
	
	private void prepareGameValues() {
		session.prepareGameValues();
		startRound();
	}
	
	private void startRound() {
		toggleButtonStatus(false);
		hideTopCards();
		session.startNewRound();
		displayAIPlayers();
		placeAIPlayersOnScreen();
		updateDisplay();
		setSeekBar();
		determineButtonDisplay();
		displayAIPlayers();
		displayGameValues();
		updateDisplay();
	}

	private void toggleButtonStatus(boolean isRoundOver) {
		Button passButton = (Button) findViewById(R.id.pass);
		Button betButton = (Button) findViewById(R.id.bet);
		Button nextButton = (Button) findViewById(R.id.nextRound);
		passButton.setEnabled(!isRoundOver);
		betButton.setEnabled(!isRoundOver);
		nextButton.setEnabled(isRoundOver);
	}
	
	private void hideTopCards() {
		ImageView flippedCardPlayer = (ImageView) findViewById(R.id.playerFlippedCard);
		ImageView flippedCardAI1 = (ImageView) findViewById(R.id.ai1flippedCard);
		ImageView flippedCardAI2 = (ImageView) findViewById(R.id.ai2flippedCard);
		ImageView flippedCardAI3 = (ImageView) findViewById(R.id.ai3flippedCard);
		ImageView flippedCardAI4 = (ImageView) findViewById(R.id.ai4flippedCard);
		ImageView flippedCardAI5 = (ImageView) findViewById(R.id.ai5flippedCard);
		flippedCardPlayer.setVisibility(View.INVISIBLE);
		flippedCardAI1.setVisibility(View.INVISIBLE);
		flippedCardAI2.setVisibility(View.INVISIBLE);
		flippedCardAI3.setVisibility(View.INVISIBLE);
		flippedCardAI4.setVisibility(View.INVISIBLE);
		flippedCardAI5.setVisibility(View.INVISIBLE);
	}
	
	private void placeAIPlayersOnScreen() {
		for(int index = 0; index < session.aiPlayerList.size(); index++) {
			determineDrawablePlayer(index, session.aiPlayerList.get(index).isKicked());
		}
	}
	
	private void updateDisplay() {
		View view = findViewById(R.id.InGameLayout);
		view.invalidate();
	}

	private void setSeekBar() {
		SeekBar betBar = (SeekBar) findViewById(R.id.betBar);
		betBar.setMax(Player.determineMaxBet(session) - 1);
		betBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				betAmount = progress + 1;
				TextView text = (TextView) findViewById(R.id.betAmount);
				text.setText(String.valueOf(betAmount));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}
	
	private void determineButtonDisplay() {
		Button betButton = (Button) findViewById(R.id.bet);
		if(session.cardsAreSameValue(session.getPlayer()) || InBetweenRules.isRangeOfCardsOne(session.getPlayer().getHand())){
			betButton.setEnabled(false);
		} else {
			betButton.setEnabled(true);
		}
	}

	private void displayAIPlayers() {
		View layoutAI1 = findViewById(R.id.ai1Hand);
		View layoutAI2 = findViewById(R.id.ai2Hand);
		View layoutAI3 = findViewById(R.id.ai3Hand);
		View layoutAI4 = findViewById(R.id.ai4Hand);
		View layoutAI5 = findViewById(R.id.ai5Hand);
		if (displayAIOne) {
			layoutAI1.setVisibility(View.VISIBLE);
		} else {
			layoutAI1.setVisibility(View.INVISIBLE);
		}
		if (displayAITwo) {
			layoutAI2.setVisibility(View.VISIBLE);
		} else {
			layoutAI2.setVisibility(View.INVISIBLE);
		}
		if (displayAIThree) {
			layoutAI3.setVisibility(View.VISIBLE);
		} else {
			layoutAI3.setVisibility(View.INVISIBLE);
		}
		if (displayAIFour) {
			layoutAI4.setVisibility(View.VISIBLE);
		} else {
			layoutAI4.setVisibility(View.INVISIBLE);
		}
		if (displayAIFive) {
			layoutAI5.setVisibility(View.VISIBLE);
		} else {
			layoutAI5.setVisibility(View.INVISIBLE);
		}
	}
	
	private void displayGameValues() {
		displayPlayerMoney();
		displayPlayerHand();
		displayAIPlayerHands();
		displayPotText();
		updateDisplay();
	}

	private void displayPlayerMoney() {
		TextView playerMoneyText = (TextView) findViewById(R.id.playerMoney);
		playerMoneyText.setText(String.valueOf(session.getPlayer().getMoney().getAmount()));
	}

	private void displayPlayerHand() {
		updateCardImage(session.getPlayer().getHand().getFirstCard(), R.id.playerCard1);
		updateCardImage(session.getPlayer().getHand().getSecondCard(), R.id.playerCard2);
	}
	
	private void displayAIPlayerHands() {
		for (int i = 0; i < session.aiPlayerList.size(); i++) {
			switch (i) {
			case 0: {
				updateCardImage(session.aiPlayerList.get(i).getHand().getFirstCard(), R.id.ai1Card1);
				updateCardImage(session.aiPlayerList.get(i).getHand().getSecondCard(), R.id.ai1Card2);
				updateAIText(i, R.id.ai1Money);
				break;
			}
			case 1: {
				updateCardImage(session.aiPlayerList.get(i).getHand().getFirstCard(), R.id.ai2Card1);
				updateCardImage(session.aiPlayerList.get(i).getHand().getSecondCard(), R.id.ai2Card2);
				updateAIText(i, R.id.ai2Money);
				break;
			}
			case 2: {
				updateCardImage(session.aiPlayerList.get(i).getHand().getFirstCard(), R.id.ai3Card1);
				updateCardImage(session.aiPlayerList.get(i).getHand().getSecondCard(), R.id.ai3Card2);
				updateAIText(i, R.id.ai3Money);
				break;
			}
			case 3: {
				updateCardImage(session.aiPlayerList.get(i).getHand().getFirstCard(), R.id.ai4Card1);
				updateCardImage(session.aiPlayerList.get(i).getHand().getSecondCard(), R.id.ai4Card2);
				updateAIText(i, R.id.ai4Money);
				break;
			}
			case 4: {
				updateCardImage(session.aiPlayerList.get(i).getHand().getFirstCard(), R.id.ai5Card1);
				updateCardImage(session.aiPlayerList.get(i).getHand().getSecondCard(), R.id.ai5Card2);
				updateAIText(i, R.id.ai5Money);
				break;
			}
			default: {
				break;
			}
			}
		}
	}

	private void displayPotText() {
		TextView potText = (TextView) findViewById(R.id.pot_text);
		potText.setText(String.valueOf(session.getPot().getPotSize()));
	}
	
	View.OnClickListener betClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			betting = true;
			toggleBetAndPassButton(true);
			takePlayerTurn();
			determineGameOver();
		}
	};

	View.OnClickListener passClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			betting = false;
			toggleBetAndPassButton(true);
			takePlayerTurn();
			determineGameOver();
		}
	};
	
	View.OnClickListener nextRoundClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (session.doesGameContinue()) {
				displayAIPlayers();
				isRoundOver = false;
				toggleBetAndPassButton(false);
				toggleButtonStatus(isRoundOver);
				displayGameValues();
				determineGameOver();
				startRound();
			} else {
				isGameOver = true;
				if (session.thereAreAIsLeft() == false) {
					TextView potText = (TextView) findViewById(R.id.pot_text);
					potText.setText("You won!");
					showVictoryDialog();
				} else {
					TextView potText = (TextView) findViewById(R.id.pot_text);
					potText.setText("GameOver");
					GAMEOVER();
				}
			}
		}
	};
	
	View.OnClickListener pauseClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent getHelp = new Intent(InGameActivity.this, PausedActivity.class);
			InGameActivity.this.startActivity(getHelp);
		}
	};

	protected void toggleBetAndPassButton(boolean flipCase) {
		Button passButton = (Button) findViewById(R.id.pass);
		Button betButton = (Button) findViewById(R.id.bet);
		passButton.setEnabled(!flipCase);
		betButton.setEnabled(!flipCase);
	}
	
	private void takePlayerTurn() {
		if (betting) {
			takeBetAction();
		}
		else{
			foldCards(R.id.playerCard1, R.id.playerCard2);
		}
		postBetAction();
		updateDisplay();
	}

	private void takeBetAction() {
		adjustMoneyAndPotForPlayerBetting();
		Card topCard = revealTopPlayerCard();
		determineIfPlayerWinsHand(topCard);
	}

	private void adjustMoneyAndPotForPlayerBetting() {
		session.getPlayer().removeMoney(betAmount);
		session.getPot().addToPot(betAmount);
		updateDisplay();
	}
	
	private Card revealTopPlayerCard() {
		Card topCard = new Card();
		topCard = Dealer.revealTopCard();
		updateCardImage(topCard, R.id.playerFlippedCard);
		return topCard;
	}
	
	private void updateCardImage(Card card, int id) {
		ImageView aCard = (ImageView) findViewById(id);
		aCard.setVisibility(View.VISIBLE);
		Drawable newCard = CardMapper.getCardImage(card, getResources());
		aCard.setImageDrawable(newCard);
	}

	private void determineIfPlayerWinsHand(Card topCard) {
		if (topCard.isBetween(session.getPlayer().getHand())) {
			session.getPlayer().addMoney(session.getPot().collectWinnings(betAmount));
		}
	}
	
	private void foldCards(int id1, int id2){
		ImageView card1 = (ImageView) findViewById(id1);
		ImageView card2 = (ImageView) findViewById(id2);
		Drawable newCard = getResources().getDrawable(R.drawable.card_back);
		card1.setImageDrawable(newCard);
		card2.setImageDrawable(newCard);
	}

	private void postBetAction() {
		prepareTimeInterval();
	}
	
	private void prepareTimeInterval() {
		int timeBlock = 0;
		int timeBetweenTurn = 750;
		for(AI_Player player: session.aiPlayerList){
			if(!player.isKicked()){
				timeBlock += 1;
			}
		}
		int totalTime = (timeBlock * timeBetweenTurn) + timeBetweenTurn;
		cycleThroughAiPlayers(totalTime,timeBetweenTurn);
	}
	
	private void cycleThroughAiPlayers(int totalTime, int blockTime) {
		new CountDownTimer(totalTime, blockTime) {
			private int index = 0;
			@Override
			public void onTick(long millisUntilFinished) {
				while(session.aiPlayerList.get(index).isKicked()){
					index++;
				}
				int betAmount = session.aiPlayerList.get(index).getBetAmount();
				if (betAmount > 0) {
					Log.i("BETTING", "Index: " + index + " Bet Amount: " + betAmount);
					Card flippedCard = Dealer.revealTopCard();
					session.takeAIPlayerTurn(betAmount, index, flippedCard);
					updateFlippedCardImage(index, flippedCard);
				} else{
					Log.i("FOLDING", "Index to fold: " + index);
					foldAI_Index(index);
				}
				determineDrawablePlayer(index, session.aiPlayerList.get(index).isKicked());
				index++;
				updateGameTextValues();
				updateDisplay();
			}
			@Override
			public void onFinish() {
				updateGameTextValues();
				isRoundOver = true;
				toggleButtonStatus(isRoundOver);
				updateDisplay();
			}
		}.start();
	}

	private void updateFlippedCardImage(int index, Card revealedCard) {
		switch (index) {
		case 0: {
			updateCardImage(revealedCard, R.id.ai1flippedCard);
			break;
		}
		case 1: {
			updateCardImage(revealedCard, R.id.ai2flippedCard);
			break;
		}
		case 2: {
			updateCardImage(revealedCard, R.id.ai3flippedCard);
			break;
		}
		case 3: {
			updateCardImage(revealedCard, R.id.ai4flippedCard);
			break;
		}
		case 4: {
			updateCardImage(revealedCard, R.id.ai5flippedCard);
			break;
		}
		default: {
			break;
		}
		}
	}
	
	private void foldAI_Index(int aiIndex){
		switch (aiIndex) {
		case 0: {
			foldCards(R.id.ai1Card1, R.id.ai1Card2);
			break;
		}
		case 1: {
			foldCards(R.id.ai2Card1, R.id.ai2Card2);
			break;
		}
		case 2: {
			foldCards(R.id.ai3Card1, R.id.ai3Card2);
			break;
		}
		case 3: {
			foldCards(R.id.ai4Card1, R.id.ai4Card2);
			break;
		}
		case 4: {
			foldCards(R.id.ai5Card1, R.id.ai5Card2);
			break;
		}
		default: {
			break;
		}
		}
	}

	private void updateGameTextValues() {
		displayPotText();
		TextView playerMoneyText = (TextView) findViewById(R.id.playerMoney);
		playerMoneyText.setText(String.valueOf(session.getPlayer().getMoney().getAmount()));
		for (int i = 0; i < session.aiPlayerList.size(); i++) {
			switch (i) {
			case 0: {
				updateAIText(i, R.id.ai1Money);
				break;
			}
			case 1: {
				updateAIText(i, R.id.ai2Money);
				break;
			}
			case 2: {
				updateAIText(i, R.id.ai3Money);
				break;
			}
			case 3: {
				updateAIText(i, R.id.ai4Money);
				break;
			}
			case 4: {
				updateAIText(i, R.id.ai5Money);
				break;
			}
			default: {
				break;
			}
			}
		}
	}
	
	private void updateAIText(int index, int id) {
		TextView aiMoneyText = (TextView) findViewById(id);
		aiMoneyText.setText(String.valueOf(session.aiPlayerList.get(index).getMoney().getAmount()));
	}
	
	private void determineDrawablePlayer(int index, boolean kickStatus) {
		switch(index) {
		case 0: {
			displayAIOne = !kickStatus;
			break;
		}
		case 1: {
			displayAITwo = !kickStatus;
			break;
		}
		case 2: {
			displayAIThree = !kickStatus;
			break;
		}
		case 3: {
			displayAIFour = !kickStatus;
			break;
		}
		case 4: {
			displayAIFive = !kickStatus;
			break;
		}
		default: {
			break;
		}}
	}
	
	private void determineGameOver()
	{
		if (!session.doesGameContinue()) 
		{
			isGameOver = true;
			if (session.thereAreAIsLeft() == false) 
			{
				TextView potText = (TextView) findViewById(R.id.pot_text);
				potText.setText("You won!");
				showVictoryDialog();
			} 
			else
			{
				TextView potText = (TextView) findViewById(R.id.pot_text);
				potText.setText("GameOver");
				GAMEOVER();
			}
		} 
	}

	private void GAMEOVER() {
		DialogFragment gameOverDialog = new GameOverDialog();
		gameOverDialog.show(getFragmentManager(), "Game Over");
	}

	private void showVictoryDialog() {
		DialogFragment victoryDialog = new GameWonDialog();
		victoryDialog.show(getFragmentManager(), "Congratulations, you won!");
	}
	
	@Override
	public void onBackPressed() {
		showLeaveGameDialog();
	}
	
	private void showLeaveGameDialog() {
		DialogFragment leaveDialog = new LeaveGameDialog();
		leaveDialog.show(getFragmentManager(), "Are you sure you want to leave?");
	}
	
}