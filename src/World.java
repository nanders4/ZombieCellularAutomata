import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class World {
	public int humans;
	public int zombies;
	
	private int[][] grid;
	private boolean[][] doneGrid; //1=done, 0=not done
	private int N;
	private static final double FIGHTPROB = .5;
	private static final double WINPROB = .55;
	private static final double BUILDPROB = .4;
	private static final double EXPANDPROB = .6;
	private static final double BIRTHPROB = .1;
	private static final int BUILDTHRESHOLD = 3; //number of humans adjacent required to build
	private static final int WALLMAX = 3; //max number walls adjacent to humans to allow building
	private static final int HUMANSTOEXPAND = 1; //number of adjacnet humans required to expand
	private static final double BREAKPROB = .001;
	
	/*
	 * 0: empty
	 * 1: human
	 * 2: zombie
	 * 3: wall
	 */
	
	public World(int N) {
		this.N = N;
		this.grid = new int[N][N];
		this.doneGrid = new boolean[N][N];
		humans = 0;
		zombies = 0;
		/*grid[5][5] = 3;
		grid[5][6] = 2;
		grid[6][5] = 0;
		grid[6][6] = 0;*/

		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				doneGrid[i][j] = false;
				double rand = Math.random();
				int value;
				if(rand<10.0/900) {
					value = 1; // Humans
					humans++;
				}
				else if(rand>=45.0/900 && rand<90./900) {
					value = 2; // Zombies
					zombies++;
				}
				else {
					value = 0; // Empty
					//3 = fortress
				}
				this.grid[i][j] = value;
			}
		}
	}
	
	public void update() {
		int closeHumans;
		int closeZombies;
		int closeWalls;
		int spot;
		int[][] neighbors;
		int[] fleePos;
		int[] birthPos;
		
		//reset done
		humans = 0;
		zombies = 0;
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				updateDoneGrid(i,j,false);
				if(grid[i][j]==1) {
					humans++;
				}
				else if(grid[i][j]==2) {
					zombies++;
				}
			}
		}
		
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				spot = grid[i][j];
				fleePos = canFlee(i,j, 1);
				birthPos = canFlee(i,j, 0);
				closeHumans = 0;
				closeZombies = 0;
				closeWalls = 0;
				neighbors = getNeighbors(i,j);
				for(int k=0; k<neighbors.length; k++) {
					if(getValueFromArray(neighbors[k]) == 1) { //is human
						closeHumans++;
					}
					else if(getValueFromArray(neighbors[k]) == 2) { //is zombo
						closeZombies++;
					}
					else if(getValueFromArray(neighbors[k]) == 3) {
						closeWalls++;
					}
				}
				
				//spot is human
				if(spot == 1 && !doneGrid[i][j]) {
					//build wall
					if(closeZombies==0 && closeHumans+1>=BUILDTHRESHOLD && closeWalls<=WALLMAX && !doneGrid[i][j]) {
						if(attemptBuildWall(i,j)) {
							doneGrid[i][j] = true;
						}
					}
					
					//miracle of birth
					if(Math.random()<=BIRTHPROB && closeHumans>=0) {
						if(birthPos[0]!=-1) {
							updateGrid(birthPos[0], birthPos[1], 1);
							updateDoneGrid(birthPos[0], birthPos[1], true);
						}
					}
					
					//fight/flee zombie
					for(int k=0; k<neighbors.length; k++) {
						int[] neighbor = neighbors[k];
						//if zombie neighbor and not done
						if(getValueFromArray(neighbor)==2 && !doneGrid[i][j]) {
							//if fight
							if(Math.random() <=FIGHTPROB /** Math.pow(BACKUPMOD+1, closeHumans-closeZombies) */|| fleePos[0]==-1) {
								//if human win
								if(Math.random()<= WINPROB /** Math.pow(BACKUPMOD+1, closeHumans-closeZombies)*/) {
									updateGrid(neighbor[0], neighbor[1], 0);
									updateDoneGrid(i,j, true);
								}
								//human lose
								else {
									updateGrid(i, j, 2);
									spot = grid[i][j];
									updateDoneGrid(i,j, true);
								}
							}
							//flee
							else {
								updateGrid(i,j, 0);
								updateGrid(fleePos[0], fleePos[1], 1);
								doneGrid[fleePos[0]][fleePos[1]] = true;
							}
						}						
					}
					
					//move randomly
					if(!doneGrid[i][j]) {
						if(fleePos[0]!=-1) {
							updateGrid(i,j, 0);
							updateGrid(fleePos[0], fleePos[1], 1);
							updateDoneGrid(i,j, true);
							updateDoneGrid(fleePos[0], fleePos[1], true);
							spot = grid[i][j];
						}
						doneGrid[i][j] = true;
					}
						
						
				}
			}
		}
		
		//zombies and wealls
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				//printWorld(i,j);
				//printDoneWorld();
				spot = grid[i][j];
				closeHumans = 0;
				closeZombies = 0;
				closeWalls = 0;
				neighbors = getNeighbors(i,j);
				for(int k=0; k<neighbors.length; k++) {
					if(getValueFromArray(neighbors[k]) == 1) { //is human
						closeHumans++;
					}
					else if(getValueFromArray(neighbors[k]) == 2) { //is zombo
						closeZombies++;
					}
					else if(getValueFromArray(neighbors[k]) == 3) {
						closeWalls++;
					}
				}
				//zombie
				if(spot==2 && !doneGrid[i][j]) {
					//attack things
					for(int k=0; k<neighbors.length; k++) {
						int[] neighbor = neighbors[k];
						if(getValueFromArray(neighbor) == 1 && !doneGrid[i][j]) {
							//if zombie lose
							if(Math.random()<= WINPROB /** Math.pow(WINPROB+1, closeHumans-closeZombies)*/) {
								updateGrid(i, j, 0);
								updateDoneGrid(i,j, true);
							}
							//zombie win
							else {
								updateGrid(neighbor[0], neighbor[1], 2);
								updateDoneGrid(i,j, true);
								updateDoneGrid(neighbor[0], neighbor[1], true);
							}
						}
						//wall
						else if(getValueFromArray(neighbor) == 3 && !doneGrid[i][j]) {
							if(Math.random()<= BREAKPROB) {
								updateGrid(i,j,0);
								updateGrid(neighbor[0], neighbor[1], 2);
								updateDoneGrid(neighbor[0], neighbor[1], true);
								updateDoneGrid(i,j,true);
							}
						}
					}
					//move randomly
					List<int[]> moves = new ArrayList<int[]>();
					//add valid moves
					for(int k=0; k<neighbors.length; k++) {
						int[] neighbor = neighbors[k];
						if(getValueFromArray(neighbor) == 0) {
							moves.add(neighbor);
						}
					}
					if(moves.size()!=0 && !doneGrid[i][j]) {
						int[] movePos = moves.get((int) Math.floor(Math.random()*moves.size()));
						updateGrid(i, j, 0);
						updateGrid(movePos[0], movePos[1], 2);
						updateDoneGrid(i, j, true);
						updateDoneGrid(movePos[0], movePos[1], true);
					}
				}
				
				//wall
				if(spot==3 && !doneGrid[i][j]) {
					if(Math.random()<=EXPANDPROB && closeHumans>=HUMANSTOEXPAND) {
						if(attemptExpand(i,j)) {
							updateGrid(i,j, 0);
						}
					}
				}	
			}
		}
	}
	
	//makes new walls at random direction if possible, sets doneGrid; Note: does not delete old wall
	private boolean attemptExpand(int x, int y) {
		List<String> expandList = new ArrayList<String>();
		int[][] neighbors = getNeighbors(x, y);
		
		//check which walls can be build
		if(!doneGrid[x][y] && canExpand(neighbors[0]) && canExpand(neighbors[1]) && canExpand(neighbors[2]) 
				&& getValueFromArray(neighbors[3]) == 3 && getValueFromArray(neighbors[4]) == 3) {
			expandList.add("N");
		}
		if(!doneGrid[x][y] && canExpand(neighbors[2]) && canExpand(neighbors[4]) && canExpand(neighbors[7])
				&& getValueFromArray(neighbors[1]) == 3 && getValueFromArray(neighbors[6]) == 3) {
			expandList.add("E");
		}
		if(!doneGrid[x][y] && canExpand(neighbors[5]) && canExpand(neighbors[6]) && canExpand(neighbors[7])
				&& getValueFromArray(neighbors[3]) == 3 && getValueFromArray(neighbors[4]) == 3) { 
			expandList.add("S");
		}
		if(!doneGrid[x][y] && canExpand(neighbors[0]) && canExpand(neighbors[3]) && canExpand(neighbors[5])
				&& getValueFromArray(neighbors[1]) == 3 && getValueFromArray(neighbors[6]) == 3) {
			expandList.add("W");
		}
		
		//actually build
		String dir = "";
		if(expandList.size()!=0) {
			dir = expandList.get((int) Math.floor(Math.random()*expandList.size()));
		}
		else {
			return false;
		}
		
		if(dir=="N") {
			updateGrid(neighbors[0][0], neighbors[0][1], 3);
			updateGrid(neighbors[1][0], neighbors[1][1], 3);
			updateGrid(neighbors[2][0], neighbors[2][1], 3);
			updateDoneGrid(neighbors[0][0], neighbors[0][1], true);
			updateDoneGrid(neighbors[1][0], neighbors[1][1], true);
			updateDoneGrid(neighbors[2][0], neighbors[2][1], true);
			return true;
		}
		else if(dir=="E") {
			updateGrid(neighbors[2][0], neighbors[2][1], 3);
			updateGrid(neighbors[4][0], neighbors[4][1], 3);
			updateGrid(neighbors[7][0], neighbors[7][1], 3);
			updateDoneGrid(neighbors[2][0], neighbors[2][1], true);
			updateDoneGrid(neighbors[4][0], neighbors[4][1], true);
			updateDoneGrid(neighbors[7][0], neighbors[7][1], true);
			return true;
		}
		else if(dir=="S") {
			updateGrid(neighbors[5][0], neighbors[5][1], 3);
			updateGrid(neighbors[6][0], neighbors[6][1], 3);
			updateGrid(neighbors[7][0], neighbors[7][1], 3);
			updateDoneGrid(neighbors[5][0], neighbors[5][1], true);
			updateDoneGrid(neighbors[6][0], neighbors[6][1], true);
			updateDoneGrid(neighbors[7][0], neighbors[7][1], true);
			return true;
		}
		else if(dir=="W") {
			updateGrid(neighbors[0][0], neighbors[0][1], 3);
			updateGrid(neighbors[3][0], neighbors[3][1], 3);
			updateGrid(neighbors[5][0], neighbors[5][1], 3);
			updateDoneGrid(neighbors[0][0], neighbors[0][1], true);
			updateDoneGrid(neighbors[3][0], neighbors[3][1], true);
			updateDoneGrid(neighbors[5][0], neighbors[5][1], true);
			return true;
		}
		else {
			return false;
		}
	}
	
	private void updateDoneGrid(int x, int y, boolean status) {
		doneGrid[x][y] = status;
	}
	
	private boolean canExpand(int[] array) {
		if(getValueFromArray(array)==0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//returns random possible flee position, if no valid pos, returns {-1,-1}, flag inclues neighborneighbors
	private int[] canFlee(int x, int y, int flag) {
		List<int[]> viable = new ArrayList<int[]>();
		boolean isViable;
		int[][] neighbors = getNeighbors(x, y);
		for(int i=0; i<neighbors.length; i++) {
			int[] neighbor = neighbors[i];
			isViable = true;
			int[][] neighborNeighbors = getNeighbors(neighbor[0], neighbor[1]);
			//if neighbor empty
			if(getValueFromArray(neighbor)==0) {
				if(flag==1) {
					for(int j=0; j<neighborNeighbors.length; j++) {
						int[] neighborNeighbor = neighborNeighbors[j];
						if(getValueFromArray(neighborNeighbor) == 2) {
							isViable = false;
						}
					}
					if(isViable) {
						viable.add(neighbor);
					}
				}
				else {
					viable.add(neighbor);
				}
			}
		}
		if(viable.size()==0) {
			return new int[] {-1,-1};
		}
		int[] ret = viable.get((int) Math.floor(Math.random()*viable.size()));
		return ret;
	}
	
	private int[][] getNeighbors(int x, int y) {
		int[][] neighbors = new int[8][2];
		neighbors[0] = new int[]{Math.floorMod((x-1), N), Math.floorMod(y-1,N)};
		neighbors[1] = new int[]{Math.floorMod((x-1), N), Math.floorMod(y,N)};
		neighbors[2] = new int[]{Math.floorMod((x-1), N), Math.floorMod(y+1,N)};
		neighbors[3] = new int[]{Math.floorMod((x), N), Math.floorMod(y-1,N)};
		neighbors[4] = new int[]{Math.floorMod((x), N), Math.floorMod(y+1,N)};
		neighbors[5] = new int[]{Math.floorMod((x+1), N), Math.floorMod(y-1,N)};
		neighbors[6] = new int[]{Math.floorMod((x+1), N), Math.floorMod(y,N)};
		neighbors[7] = new int[]{Math.floorMod((x+1), N), Math.floorMod(y+1,N)};
		return neighbors;
	}
	
	private void updateGrid(int x, int y, int value) {
		grid[x][y] = value;
	}
	
	private boolean attemptBuildWall(int x, int y) {
		List<int[]> humanList = new ArrayList<int[]>();
		int wallCount;
		humanList.add(new int[]{x,y});
		boolean wallOkay = true;
		int[][] neighbors = getNeighbors(x, y);
		for(int i=0; i<neighbors.length; i++) {
			wallCount = 0;
			int[] neighbor = neighbors[i];
			if(getValueFromArray(neighbor) == 2) {
				wallOkay = false;
				break;
			}
			else if(getValueFromArray(neighbor) == 1) {
				wallOkay = buildWallUtil(neighbor[0], neighbor[1], humanList);
				if(!wallOkay) {
					break;
				}
			}
			else if(getValueFromArray(neighbor) == 3) {
				wallCount++;
			}
			if(wallCount>WALLMAX) {
				wallOkay = false;
				break;
			}
		}
		if(wallOkay && Math.random()<=BUILDPROB) {
			humanList = new ArrayList<int[]>();
			humanList.add(new int[] {x,y});
			for(int i=0; i<neighbors.length; i++) {
				int[] neighbor = neighbors[i];
				if(getValueFromArray(neighbor)==0) {
					updateGrid(neighbor[0], neighbor[1], 3);
					updateDoneGrid(neighbor[0], neighbor[1], true);
				}
				else if(getValueFromArray(neighbor)==1) {
					trueBuildWall(neighbor[0], neighbor[1], humanList);
				}
			}
			return true;
		}
		return false;
	}
	
	private void trueBuildWall(int x, int y, List<int[]> humanList) {
		humanList.add(new int[] {x,y});
		int[][] neighbors = getNeighbors(x, y);
		for(int i=0; i<neighbors.length; i++) {
			int[] neighbor = neighbors[i];
			if(!listContains(humanList, neighbor)) {
				if(getValueFromArray(neighbor)==0) {
					updateGrid(neighbor[0], neighbor[1], 3);
					updateDoneGrid(neighbor[0], neighbor[1], true);
				}
				else if(getValueFromArray(neighbor)==1) {
					trueBuildWall(neighbor[0], neighbor[1], humanList);
				}
			}
		}
	}
	
	private boolean buildWallUtil(int x, int y, List<int[]> humanList) {
		int wallCount;
		humanList.add(new int[] {x, y});
		int[][] neighbors = getNeighbors(x, y);
		for(int i=0; i<neighbors.length; i++) {
			wallCount = 0;
			int[] neighbor = neighbors[i];
			if(!listContains(humanList, neighbor)) {
				if(getValueFromArray(neighbor) == 2) {
					return false;
				}
				else if(getValueFromArray(neighbor) == 1) {
					if(!buildWallUtil(neighbor[0], neighbor[1], humanList)) {
						return false;
					}
				}
				else if(getValueFromArray(neighbor) == 3) {
					wallCount++;
				}
			}
			if(wallCount>WALLMAX) {
				return false;
			}
		}
		return true;
	}
	
	private int getValueFromArray(int[] array) {
		return grid[array[0]][array[1]];
	}
	
	private boolean listContains(List<int[]> list, int[] coord) {
		for(int i=0; i<list.size(); i++) {
			if(list.get(i)[0] == coord[0] && list.get(i)[1] == coord[1]) {
				return true;
			}
		}
		return false;
	}
	
	public void printWorld() {
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				if(grid[i][j]==0) {
					System.out.print(".");
				}
				else if(grid[i][j]==1) {
					System.out.print("☺");
				}
				else if(grid[i][j]==2) {
					System.out.print("Z");
				}
				else if(grid[i][j]==3) {
					System.out.print("■");
				}
				if(j==N-1) {
					System.out.print("\n");
				}
			}
		}
		System.out.print("\n");
	}
	public void printWorld(int x, int y) {
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				if(i == x && j == y) {
					System.out.print("X");
				}
				else if(grid[i][j]==0) {
					System.out.print(".");
				}
				else if(grid[i][j]==1) {
					System.out.print("☺");
				}
				else if(grid[i][j]==2) {
					System.out.print("Z");
				}
				else if(grid[i][j]==3) {
					System.out.print("■");
				}
				if(j==N-1) {
					System.out.print("\n");
				}
			}
		}
		System.out.print("\n");
	}
	
	public void printDoneWorld() {
		for(int i=0; i<N; i++) {
			for(int j=0; j<N; j++) {
				if(doneGrid[i][j]==true) {
					System.out.print("1");
				}
				else {
					System.out.print("0");
				}
				if(j==N-1) {
					System.out.print("\n");
				}
			}
		}
		System.out.print("\n");
	}
	
	public void printClear() {
		for(int i=0; i<30; i++) {
			System.out.println();
		}
	}
	
}
