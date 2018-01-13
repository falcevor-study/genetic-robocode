package falcevor;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;

import java.io.IOException;

public class Falcevor extends AdvancedRobot {
	private Heritage _heritage;
	private ScannedRobotEvent _lastState;
	private AdvancedEnemyBot enemy = new AdvancedEnemyBot();

	private boolean _isTraining = true;
	
	public static int _shots = 0;
	public static int _hits = 0;
	public static int _enemy_shots = 0;
	public static int _enemy_hits = 0;
	public static int _wall_colides = 0;
	public static int _enemy_colides = 0;

	public void run() {
		try {
			_heritage = new Heritage(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
      	setAdjustGunForRobotTurn(true);
      	setAdjustRadarForGunTurn(true);
		
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		while (true) {
			
			setTurnGunRight(getGunTurnAngle());
			
			if (getGunHeat() == 0) {
				setFire(getBulletPower());
				_shots++;
			}
			
			setAhead(getMoveDistance());
			
			setTurnRight(getTurnAngle());
			scan();
			execute();
		}
	}
	
	public void onHitByWall(HitByBulletEvent e) {
		_wall_colides++;
    }
	
	public void onHitRobot(HitRobotEvent e) {
		_enemy_colides++;
	}

	public void onBulletHit(BulletHitEvent e) {
		_hits++;
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		_enemy_hits++;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if ( enemy.none() || e.getDistance() < enemy.getDistance() - 70 ||
			e.getName().equals(enemy.getName())) {
			enemy.update(e, this);
		}
		_heritage.updateEnemy(e);
		_lastState = e;
   		setTurnRadarRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians()));
	}
	
	public void onRoundEnded(RoundEndedEvent e) {
		if (_isTraining) {
			try {
				RobocodeFileOutputStream fos = new RobocodeFileOutputStream(getDataFile("result.txt"));
				StringBuilder output = new StringBuilder();
				output.append(_shots).append(" ").append(_hits).append(" ")
					  .append(_enemy_shots).append(" ").append(_enemy_hits).append(" ")
                      .append(_wall_colides).append(" ").append(_enemy_colides);
				fos.write(output.toString().getBytes());
				fos.close();
			} catch (IOException ex) {
				_isTraining = false;
			}
		} 
	}
	
	private double getTurnAngle() {
		return Math.toDegrees(_heritage.getFunctionValue(0));
	}
	
	private double getGunTurnAngle() {
		double bulletSpeed = 20 - getBulletPower() * 3;
		long time = (long)(enemy.getDistance() / bulletSpeed);
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
		return normalizeBearing(absDeg - getGunHeading());
		//return Math.toDegrees(_heritage.getFunctionValue(1));
	}
	
	private double getMoveDistance() {
		return _heritage.getFunctionValue(2);
	}
	
	private double getBulletPower() {
		return Math.min(400 / _lastState.getDistance(), 3);
	}
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;
	
		if (xo > 0 && yo > 0) {
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) {
			bearing = 360 + arcSin; 
		} else if (xo > 0 && yo < 0) { 
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { 
			bearing = 180 - arcSin; 
		}
		return bearing;
	}
	
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
}
 
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																