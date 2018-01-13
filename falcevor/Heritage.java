package falcevor;

import robocode.*;
import robocode.util.Utils;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Heritage {
	private Enemy _enemy;
	private AdvancedRobot _robot;	

	private static final int CHROMOSOME_COUNT = 3;
	private static final int GENE_COUNT = 201;
	private static final int MAX_ARITY = 4;
	private static final int[] ARITY = {
 										1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 
										2, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
										0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
										0
									   };
						   
	private int[][] _chromosomes;
	private int[][][] _functionTree;


	public Heritage(AdvancedRobot robot) throws Exception {
		this._enemy = new Enemy();
		this._robot = robot;
		
		_chromosomes = new int[CHROMOSOME_COUNT][GENE_COUNT];
		_functionTree = new int[CHROMOSOME_COUNT][GENE_COUNT][MAX_ARITY];
		
		readChromosomes();
		buildTree();
	}
	

	/**
	 * Считать массивы хромосом из файла.
	 */
	private void readChromosomes() throws Exception {
		try (BufferedReader r = new BufferedReader(new FileReader(_robot.getDataFile("chromosome.dat")))) {	
			for (int i = 0; i < CHROMOSOME_COUNT; ++i) {
				_chromosomes[i] = 
					Arrays.stream(r.readLine().split(" ")).mapToInt(
						(chr) -> Integer.parseInt(chr)
					).toArray();
			}
		}
	}
	

	/**
	 * Построить дерево функций, исходя из массивов хромосом.
	 */
	private void buildTree() {
		for (int i = 0; i < CHROMOSOME_COUNT; ++i) {
			int[][] tree = _functionTree[i];
			int[] chromosome = _chromosomes[i]; 
			int link = 1;
			
			for (int gene = 0; gene < GENE_COUNT; ++gene) {
				for (int operand = 0; operand < ARITY[chromosome[gene]] && link < GENE_COUNT; ++operand)
					tree[gene][operand] = link++;
			}
		}		
	}
	
	
	/**
	 * Обновить данные о противнике, исходя из события обнаружения.
	 */
	public void updateEnemy(ScannedRobotEvent e) {
		double a = Utils.normalRelativeAngle(_robot.getHeadingRadians() + e.getBearingRadians());
		_enemy.X = _robot.getX() + e.getDistance() * Math.cos(a);
		_enemy.Y = _robot.getY() + e.getDistance() * Math.sin(a);
		_enemy.PREVIOUS_ENERGY = _enemy.ENERGY;
		_enemy.ENERGY = e.getEnergy();
		_enemy.PREVIOUS_DISTANCE = _enemy.DISTANCE;
		_enemy.DISTANCE = e.getDistance();
		_enemy.PREVIOUS_HEADING = _enemy.HEADING;
		_enemy.HEADING = e.getHeadingRadians();
		_enemy.PREVIOUS_VELOCITY = _enemy.VELOCITY;
		_enemy.VELOCITY = e.getVelocity();
		_enemy.BEARING = e.getBearingRadians();
		
		double delta = _enemy.PREVIOUS_ENERGY - _enemy.ENERGY;
		if (delta > 0.099 && delta < 3.001) { Falcevor._enemy_shots++; }
	}
	

	/**
	 * Получить значение генетической функции.
	 */
	public double getFunctionValue(int funcNumber) {
		return getTreeValue(_chromosomes[funcNumber], _functionTree[funcNumber], 0);
	}


	/**
	 * Получить значение функции из дерева, основанного на хромосомах.
	 */
	public double getTreeValue(int[] chromosome, int[][] functionTree, int gene) {
		double[] operands = new double[MAX_ARITY];
		
		for (int operandNumber = ARITY[chromosome[gene]]-1; operandNumber >= 0; --operandNumber) {
			operands[operandNumber] = getTreeValue(chromosome, functionTree, functionTree[gene][operandNumber]);
		}
		
		switch(chromosome[gene]) {
			case 0:  return -operands[0];
			case 1:  return Math.pow(-1, Math.round(operands[0]));
			case 2:  return Math.abs(operands[0]);
			case 3:  return Math.acos(operands[0]);
			case 4:  return Math.asin(operands[0]);
			case 5:  return Math.atan(operands[0]);
			case 6:  return Math.atan2(operands[0], operands[1]);
			case 7:  return Math.sqrt(operands[0]);
			case 8:  return Math.cbrt(operands[0]);
			case 9:  return Math.cos(operands[0]);
			case 10: return Math.sin(operands[0]);
			case 11: return Math.tan(operands[0]);
			case 12: return operands[0] > 0? Math.log(operands[0]) : 0;
			case 13: return operands[0] + operands[1];
			case 14: return operands[0] * operands[1];
			case 15: return operands[1] != 0? operands[0] / operands[1] : 0;
			case 16: return Math.pow(operands[0], operands[1]);
			case 17: return Math.max(operands[0], operands[1]);
			case 18: return Math.min(operands[0], operands[1]);
			case 19: return operands[0] > operands[1] + 0.01 && operands[0] < operands[2] - 0.01? 1 : -1;
			case 20: return operands[0] > operands[1] + 0.01 ? operands[2] : operands[3];
			case 21: return 0.0;
			case 22: return 0.1;
			case 23: return -0.1;
			case 24: return 0.33;
			case 25: return -0.33;
			case 26: return 0.5;
			case 27: return -0.5;
			case 28: return 1;
			case 29: return -1;
			case 30: return 10;
			case 31: return -10;
			case 32: return Math.PI;
			case 33: return -Math.PI;
			case 34: return _robot.getBattleFieldHeight();
			case 35: return _robot.getBattleFieldWidth();
			case 36: return _robot.getEnergy();
			case 37: return _robot.getGunHeading();
			case 38: return _robot.getHeading();
			case 39: return _robot.getVelocity();
			case 40: return _robot.getX();
			case 41: return _robot.getY();
			case 42: return _enemy.X;
			case 43: return _enemy.Y;
			case 44: return _enemy.ENERGY;
			case 45: return _enemy.PREVIOUS_ENERGY;
			case 46: return _enemy.DISTANCE;
			case 47: return _enemy.PREVIOUS_DISTANCE;
			case 48: return _enemy.HEADING;
			case 49: return _enemy.PREVIOUS_HEADING;
			case 50: return _enemy.VELOCITY;
			case 51: return _enemy.PREVIOUS_VELOCITY;	
			default: return 0;
		}	
	}
}
