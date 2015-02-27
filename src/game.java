import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

class Direction {
	static int NORTH = 1;
	static int EAST = 2;
	static int SOUTH = 3;
	static int WEST = 4;
}

class Ball {
	Size size = new Size( 5, 5 );
	Pos pos = new Pos( 400, 500 );
	Pos velocity = new Pos( 0, -5 );
}

class Brick {
	Size size = new Size( 50, 20 );
	Pos pos = new Pos( 0, 0 );
	Color color = (new Random().nextInt(2) == 1) ? Color.RED : Color.BLUE;
}

class Bar {
	Size size = new Size( 120, 10 );
	Pos pos = new Pos( 30, 0 );
	Pos velocity = new Pos( 0, 0 );
}

class Size {
	int w, h;
	
	Size( int w, int h ) {
		this.w = w;
		this.h = h;
	}
}

class Pos {
	int  x, y;
	double degradance = 0;
	
	Pos( int x, int y ) {
		this.x = x;
		this.y = y;
	}
	
	public void centerHoriz( int w, Size s ) {
		this.x = ( w / 2 ) - ( s.w / 2 );
	}
	
	public void move( Pos velocity, boolean changeVelocity, boolean collideWithBar, Size s ) {
		int newX =  (this.x + velocity.x), newY = (this.y + velocity.y);
		
		// Gravity
		if( collideWithBar )
			velocity.y = (velocity.y == 0) ? -1 : velocity.y;
		
		// Boundary collision checking
		if( velocity.x != 0 || velocity.y != 0 ) {
			boolean terminate = false;
			
			if( (newX + s.w) > game.WIDTH || 0 > newX ) { velocity.x = velocity.x * -1; terminate = true; }
			else if( (newY + s.h) > game.HEIGHT || 0 > newY ) {velocity.y = velocity.y * -1; terminate = true; }
			
			if( terminate ) {
				if( changeVelocity ) {
					velocity.x += new Random().nextInt(2) - 1;
					velocity.y += new Random().nextInt(2) - 1;
					move( velocity );
				}
				
				return;
			}
		}
		
		/*
		 * Note, there might be a bug with the collision portion of the physics system
		 * due to the order that I am calling the operations below. I don't know yet.
		 */
		
		this.x = newX;
		this.y = newY;
		degradance += 0.05;
		
		// Slow down over time
		if( degradance >= 1 && collideWithBar ) {
			if( Math.abs( velocity.x ) > 5 ) velocity.x += (velocity.x > 0) ? -1 : 1;
			if( Math.abs( velocity.y ) > 5 ) velocity.y += (velocity.y > 0) ? -1 : 1;
			degradance = 0.00;
		}
		
		// Bar collision checking
		if( this.isWithin( game.bar.pos, game.bar.size ) ) {
			velocity.y = velocity.y * -1 + game.bar.velocity.y;
			velocity.x += game.bar.velocity.x;
			move( velocity, changeVelocity, collideWithBar, s );
		}
		
		//Object collision checking
		for (int i = 0; i <1000 ; i++)
		{
			Brick a = game.bricks[i];
		// for( Brick b : game.bricks ) 
		// {
			 if (a!= null){
				 if(this.isWithin(a.pos, a.size))
			 	{
					//if the ball hits the side of the block
					if (this.isSide(a.pos, a.size))
					{
						velocity.x = velocity.x * -1;
					 	move( velocity, changeVelocity, collideWithBar, s );
					}
					// if hits top or bottom of a block
					else {
				 	
				 	velocity.y = velocity.y * -1;
				 	move( velocity, changeVelocity, collideWithBar, s );
					}
				 	
				 	
				 	
				 	Brick[] copy = new Brick[ game.bricks.length ];

				 	//deletes brick when hit
				 	for (int k = 0; k < i; k ++ )
				 	{
				 		copy[k] = game.bricks[k];
				 	}
				 	for (int j = i+1; j <game.bricks.length; j++ )
				 	{
				 		copy[j-1] = game.bricks[j];
				 	}
				 	game.bricks = copy;
				 	i--;
			 	}
			 }
		 }

		
	}
	
	public void move( Pos velocity ) {
		this.move( velocity, true, true, new Size( 0, 0 ) );
	}
	
	// Collisions with objects not perfect fix
	public boolean isSide (Pos pos, Size bounds){
		if ((Math.abs(this.x - pos.x) <= 3) || (Math.abs(pos.x+bounds.w - this.x)<=3))
		{
			return true;
		}
		return false;
	}
	//public boolean isBotorTop(Pos pos, Size bounds){
				
	//}
	public boolean isWithin( Pos pos, Size bounds ) {
		if( (this.x > pos.x && ((pos.x + bounds.w) > this.x)) &&
			(this.y > pos.y && ((pos.y + bounds.h) > this.y)) )
			return true;
		
		return false;
	}
}

class renderThread extends Thread {

	public void run() {
		JFrame frame = new JFrame( "Breakout" );
		
		frame.addWindowListener( new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			
		} );
		
		final JPanel mainPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Background
                g.setColor( Color.decode( "#CCCCCC" ) );
                g.fillRect( 0, 0, 800, 500 );
                
                // Draw the ball
                g.setColor( Color.BLACK );
                g.fillRect(game.ball.pos.x - Math.floorDiv(game.ball.size.w, 2), game.ball.pos.y - Math.floorDiv(game.ball.size.h, 2), game.ball.size.w, game.ball.size.h);
                
                // Draw the bricks
                for( Brick b : game.bricks ) {
                	if( b != null ) {
                		g.setColor( b.color );
                		g.fillRect( b.pos.x, b.pos.y, b.size.w, b.size.h );
                	}
                }
                
                // Draw the bar
                g.setColor( Color.BLACK );
                g.fillRect( game.bar.pos.x, (int) game.bar.pos.y, game.bar.size.w, game.bar.size.h );
                
                g.setColor( Color.BLUE );
                g.fillRect( game.bar.pos.x + 1, (int) game.bar.pos.y + 1, game.bar.size.w - 2, game.bar.size.h - 2 );
            }
		};
		
		mainPanel.setPreferredSize( new Dimension(game.WIDTH, game.HEIGHT) );
		
		frame.setLayout( new BorderLayout() );
		frame.add( mainPanel, BorderLayout.CENTER );
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		frame.addKeyListener( new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				if( e.getKeyCode() == 37 || e.getKeyCode() == 39 ) {
		    		game.bar.velocity.x = ( e.getKeyCode() == 39 ) ? 10 : -10;
		    	}
		    	
		    	mainPanel.repaint();
			}
			
			public void keyReleased(KeyEvent e) {
		    	if( e.getKeyCode() == 37 || e.getKeyCode() == 39 ) {
		    		game.bar.velocity.x = 0;
		    	}
		    	
		    	mainPanel.repaint();
		    }
		    
			
			public void keyTyped(KeyEvent e) {}
		} );
		
		// repaint watcher
		while( true ) {
			if( game.REPAINT ) {
				frame.repaint();
				game.REPAINT = false;
			}
			
			try {
				sleep( game.SLEEP_DELAY );
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}

class calcThread extends Thread {
	
	public void run() {
			while( true ) {
			try {
				// Update velocities
				game.bar.pos.move( game.bar.velocity, false, false, game.bar.size );
				game.ball.pos.move( game.ball.velocity );
				
				// System Stuff
				game.REPAINT = true;
				sleep( game.SLEEP_DELAY );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}

public class game {
	
	static int WIDTH = 800;
	static int HEIGHT = 500;
	static int SLEEP_DELAY = 30;
	static boolean REPAINT = false;
	
	static Brick[] bricks = new Brick[ 1000 ];
	static Ball ball = new Ball();
	static Bar bar = new Bar();
	
	public static void main(String args[]) {
		// Position the bar
		game.bar.pos.centerHoriz(game.WIDTH, game.bar.size);
		game.bar.pos.y = HEIGHT - game.bar.size.h - 5;
		
		// Place the bricks
		int x = 20, y = 10, i = 0;
		
		while( (game.WIDTH - x) > 100 ) {
			while( (game.HEIGHT - y) > 200 ) {
				Brick newBrick = new Brick();
				newBrick.pos = new Pos( x, y );
				bricks[ i++ ] = newBrick;
				y += newBrick.size.h +30 ;
			}
			
			x += 100;
			y = 10;
		}
		
		// Create a thread to render in
		new renderThread().start();
		
		// Create a thread to do calculations in
		new calcThread().start();
	}
	
}
