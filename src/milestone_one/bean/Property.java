package milestone_one.bean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Property {
	private static Properties prop;
	private static InputStream input;
	private static final Logger log = Logger.getLogger(Property.class.getName());
	private static String ioException = "IOException in Property.";
	private static String fileNotFoundException = "FileNotFoundException in Property.";
	
	private Property() {
		super();
	}
	
	public static Properties getInstance(){ 
        if (prop == null) {
            prop = new Properties();
        }
        if (input == null) {
        	try {
				input = new FileInputStream("resources\\configuration");
				prop.load(input);
			} catch (FileNotFoundException e) {
				log.log(Level.SEVERE,fileNotFoundException, e);
			} catch (IOException e) {
				log.log(Level.SEVERE,ioException, e);
			}
        	
        }
        return prop; 
    } 
}
