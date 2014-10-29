package io.github.repir.tools.hadoop.TSV;

import com.google.gson.Gson;
import io.github.repir.tools.Structure.StructuredRecordFile;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Generic Writable that serializes objects using Json. This approach is
 * slower than Structured.Writable (roughly 1.5 times), but through Json
 * provides an easy mechanism for complex structures and slightly more easy
 * coding since no readFields and write are generic. However, serialization does
 * require getType and getAttributes.
 * <p/>
 * Because this class is used for serialization of records, it should only contain
 * class variables that contain attributes. Static variables are not serialized. 
 * @author jeroen
 * @param <F> 
 */
public abstract class Writable<F extends StructuredRecordFile> implements io.github.repir.tools.hadoop.Writable<F> {
    private static Gson gson = new Gson();
    
    public Writable() {
    }
    
    @Override
    public void write(DataOutput d) throws IOException {
       String content = gson.toJson(this, getType());
       d.writeUTF(content);  
    }
    
    /**
     * Should return a Reflection.Type that corresponds to the actual class
     * being serialized. A type is best obtained through Gson's TypeToken
     * e.g. new TypeToken<ComplexClass<Generic>>(){}.getType(), which can be
     * stored in a static and simply returned. By using a type, this supports
     * serialization of generic classes.
     * @return 
     */
    protected abstract Type getType();

    /**
     * Hadoop requires an empty parameter and a readFields() method to deserialize
     * objects. The generic readFields in TSV.Writable reads the Json record
     * and then requires getAttributes to map the attributes to the constructed
     * variable.
     * @param o 
     */
    protected abstract void getAttributes(Object o);

    @Override
    public void readFields(DataInput di) throws IOException {
        String content = di.readUTF();
        getAttributes(gson.fromJson(content, getType()));
    }
}
