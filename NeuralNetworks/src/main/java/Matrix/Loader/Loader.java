package Matrix.Loader;

public interface Loader {
	MetaData open();
	void close();
	
	MetaData getMetaData();
	BatchData readBatch();
}
