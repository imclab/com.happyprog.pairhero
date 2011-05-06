package com.happyprog.pairhero.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.happyprog.pairhero.Activator;
import com.happyprog.pairhero.actions.StartAction;
import com.happyprog.pairhero.actions.StopAction;
import com.happyprog.pairhero.game.Game;
import com.happyprog.pairhero.game.Programmer;
import com.happyprog.pairhero.subscribers.JUnitSubscriber;
import com.happyprog.pairhero.subscribers.RefactoringSubscriber;
import com.happyprog.pairhero.time.TimeFormatter;
import com.happyprog.pairhero.time.Timer;

public class MainView extends ViewPart {

	public static final String ID = "com.happyprog.pairhero.views.MainView";

	private Label timerLabel;
	private Label scoreLabel;
	private Label messageLabel;
	private int messageDelayCounter;

	private Programmer leftProgrammer;
	private Programmer rightProgrammer;

	private Composite parent;

	private StartAction startButton;

	private Game game;

	private StopAction stopButton;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		createStartButton();
		leftProgrammer = new Programmer(parent);
		createScoreboard(parent);
		rightProgrammer = new Programmer(parent);

		parent.layout();
	}

	private void createScoreboard(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(createLayout());

		new Label(group, SWT.NONE).setText("Score:");
		scoreLabel = new Label(group, SWT.NONE);
		scoreLabel.setText("0");

		messageLabel = new Label(group, SWT.NONE);
		messageLabel.setImage(Activator.getImageDescriptor("icons/blank.png").createImage());

		timerLabel = new Label(group, SWT.NONE);
		timerLabel.setText(TimeFormatter.formatTime(Timer._25_MINS));
	}

	private RowLayout createLayout() {
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		layout.pack = true;
		layout.justify = false;
		layout.type = SWT.VERTICAL;
		return layout;
	}

	private void createStartButton() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

		startButton = new StartAction(this);
		stopButton = new StopAction(this);
		stopButton.setEnabled(false);

		toolbarManager.add(startButton);
		toolbarManager.add(stopButton);
	}

	public void onStart() {
		if (ableToCreatePlayers()) {
			startGame();
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		}
		parent.layout();
	}

	private void startGame() {
		game = new Game(this, new Timer(), leftProgrammer, rightProgrammer, new JUnitSubscriber(),
				new RefactoringSubscriber());
		game.start();
	}

	private boolean ableToCreatePlayers() {
		StartDialog dialog = new StartDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.open();

		if (dialog.getReturnCode() == Dialog.OK) {
			leftProgrammer.resetStats();
			rightProgrammer.resetStats();
			leftProgrammer.setName(dialog.getPlayerOneName());
			rightProgrammer.setName(dialog.getPlayerTwoName());

			return true;
		}

		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void onGameFinished(String message) {
		EndDialog dialog = new EndDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), message);
		dialog.open();
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}

	public void onTimeChange(int timeInSeconds) {
		updateScore(timerLabel, TimeFormatter.formatTime(timeInSeconds));
		updateMessageToDefault();
	}

	private void updateScore(final Label label, final String text) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				label.setText(text);
			}
		});
	}

	public void updateScore(int score) {
		updateScore(scoreLabel, String.format("%d", score));
		updateMessage();
	}

	public void onStop() {
		boolean response = MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Pair Hero", "Are you sure you want to stop this session?");

		if (response) {
			game.stop();
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}

	private void updateMessageToDefault() {
		if (messageDelayCounter < 0) {
			updateMessage(messageLabel, Activator.getImageDescriptor("icons/blank.png").createImage());
		}
		messageDelayCounter--;
	}

	private void updateMessage() {
		updateMessage(messageLabel, Activator.getImageDescriptor("icons/great.png").createImage());
		messageDelayCounter = 3;
	}

	private void updateMessage(final Label label, final Image image) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				label.setImage(image);
			}
		});
	}

	public void onSwitchRole() {
		updateMessage(messageLabel, Activator.getImageDescriptor("icons/start.gif").createImage());
		messageDelayCounter = 3;
	}
}