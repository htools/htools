package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.Datafile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StructuredTextTSVTest {
    static Datafile datafile;
    ExampleFile file;
    String names[] = { "aap", "noot" };
    double numbers[] = { 1.0, 2.0 };
    
    @BeforeClass
    public static void setUpClass() {
        datafile = new Datafile("StructuredTextTSVexamplefile");
    }
    
    @AfterClass
    public static void tearDownClass() {
        datafile.delete();
    }
    
    @Before
    public void setUp() {
       file = new ExampleFile(datafile);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
        file.openWrite();
        for (int i = 0; i < names.length; i++) {
            file.name.set( names[i] );
            file.number.set( numbers[i] );
            file.write();
        }
        file.closeWrite();
        file.openRead();
        int row = 0;
        while (file.nextRecord()) {
            assertEquals(names[row], file.name.get());
            assertEquals(numbers[row++], file.number.get(), 0.001);
        }
        file.closeRead();
    }
    
    class ExampleFile extends StructuredTextTSV {
        StringField name = this.addString("name");
        DoubleField number = this.addDouble("number");
        
        public ExampleFile(Datafile writer) {
            super(writer);
        }
    }
    
}
