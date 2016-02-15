package io.github.htools.hadoop.xml;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextTSV;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;
import org.junit.Before;
import org.junit.Test;

public class TimeSenseReaderTest {

    public static Log log = new Log(TimeSenseReaderTest.class);
    TimeSenseReader tsr;

    @Before
    public void setUp() {
        Datafile df = new Datafile("/Users/jeroen/Downloads/query_recency.txt");
        tsr = new TimeSenseReader(df);
    }

    @Test
    public void testValidRecord() {
//        tsr.openRead();
//        while (tsr.next()) {
//            log.printf("%s", tsr.query.get());
//        }
    }

    public class TimeSenseReader extends StructuredTextTSV {

        public FolderNode top = this.createRoot();
        public StringField query = this.addString("query");

        public TimeSenseReader(Datafile writer) {
            super(writer);
        }

        @Override
        protected boolean validRecord(ByteSearchSection section) {
            return super.validRecord(section) && section.byteAt(0) != '#';
        }
    }
}
