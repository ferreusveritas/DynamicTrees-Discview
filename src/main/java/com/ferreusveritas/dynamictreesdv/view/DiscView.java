package com.ferreusveritas.dynamictreesdv.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DiscView implements Runnable {
	
	public Thread viewerThread;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	public JFrame window;
	public ViewerGrid gridView;
	
	private int targetTickMillis;
	
	public DiscView() {
		window = new JFrame("Disc Viewer?");
		gridView = new ViewerGrid();
		JPanel container = new JPanel();
		JPanel menu = new JPanel();
		JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		gridPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		menu.setBorder(BorderFactory.createEtchedBorder());
		menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
		container.add(menu, 0);
		container.add(gridPanel, 1);
		gridPanel.add(gridView);
		menu.setAlignmentY(0.5F);
		gridPanel.setAlignmentY(0.5F);
		
		JButton startButton = new JButton("Start Generator Thread");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.startThread();
			}
		});
		JButton stopButton = new JButton("Stop Generator Thread");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.stopThread();
			}
		});
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.clear();
			}
		});
		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.generate();
			}
		});
		JButton stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.step();
			}
		});
		
		
		JSlider speedSlider = new JSlider(10, 30, 20);
		speedSlider.setBorder(BorderFactory.createTitledBorder("Game Speed"));
		speedSlider.setInverted(true);
		speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					targetTickMillis = (int) Math.pow(10.0D, source.getValue() / 10.0D);
				}
			}
		});
		targetTickMillis = (int) Math.pow(10.0D, speedSlider.getValue() / 10.0D);
		
		JPanel seedPanel = new JPanel();
		seedPanel.setBorder(BorderFactory.createTitledBorder("Random Seed:"));
		TextField seedInput = new TextField("", 19);
		seedInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gridView.setSeed(seedInput.getText());
			}
		});
		seedPanel.add(seedInput);
		
		menu.add(seedPanel);
		menu.add(speedSlider);

		menu.add(startButton);
		menu.add(stopButton);
		
		menu.add(clearButton);
		menu.add(generateButton);
		menu.add(stepButton);
		
		
		menu.add(Box.createRigidArea(new Dimension(1, 335)));
		
		window.setContentPane(container);
		window.pack();
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.pack();
		window.setResizable(false);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing Viewer");
				stop();
			}
		});
		
		viewerThread = new Thread(this);
		viewerThread.start();
	}
	
	@Override
	public void run() {
		running.set(true);
		
		//Let the window manifest itself before we start drawing. btw, THIS SUCKS!
		try { Thread.sleep(100); }
		catch (InterruptedException e) { }
		
		int renderTickMillis = 1000 / 60;
		
		int prevTime = (int) System.currentTimeMillis();
		int drawAccumulator = 0;
		int tickAccumulator = 0;
		
		
		while (running.get()) {
			int time = (int) System.currentTimeMillis();
			int delta = time - prevTime;
			prevTime = time;
			tickAccumulator += delta;
			drawAccumulator += delta;
			
			gridView.doInput();
			
			if (tickAccumulator >= targetTickMillis) {
				tickAccumulator = Math.min(tickAccumulator - targetTickMillis, targetTickMillis);
				gridView.update();
			}
			if (drawAccumulator >= renderTickMillis) {
				drawAccumulator -= renderTickMillis;
				gridView.draw();
			}
			
		}
	}
	
	public void stop() {
		running.set(false);
		gridView.stopThread();
	}
	
}
