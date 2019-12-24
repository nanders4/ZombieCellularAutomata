import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class zombieModel {
	public static void main(String[] argv) throws InterruptedException, IOException {
		World world = new World(30);
		
		if(true) {
			for(int i=0; i<1000; i++) {
				System.out.println(i);
				System.out.println("H: "+world.humans+" || Z :"+world.zombies);
				world.printWorld();
				world.update();
				Thread.sleep(100);
				world.printClear();
			}
		}
		else {
			FileWriter fileWriter = new FileWriter("output4.txt");
			PrintWriter printWriter = new PrintWriter(fileWriter);
			int countDown = 10;
			for(int i=0; i<20000; i++) {
				//System.out.println(i);
				//System.out.println("H/Z: "+(float)world.humans/world.zombies);
				//world.printWorld();
				world.update();
				//Thread.sleep(100);
				//world.printClear();
				if(i%10==0) {
					printWriter.printf("%d, %d, %d \n", i, world.humans, world.zombies);
				}
				if(world.humans==0 || world.zombies==0) {
					countDown--;
					if(countDown<=0) {
						break;
					}
				}
			}
			printWriter.close();
		}
		System.out.println((float)world.humans/world.zombies);
	}

}
