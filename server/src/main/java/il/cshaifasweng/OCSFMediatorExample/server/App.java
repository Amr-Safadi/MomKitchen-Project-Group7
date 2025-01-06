package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Meals;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static SimpleServer server;
    public static void main( String[] args ) throws IOException
    {
        server = new SimpleServer(3000);
        server.listen();
        System.out.println("Server is listening...");

        //test
        //server.updateMeal(new Meals(5,"Chocolate Cake", "Flour, Cocoa, Sugar, Eggs", "Extra Frosting, Vegan Option", 80.00));


    }
}
