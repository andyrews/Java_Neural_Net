package Matrix.Loader.image;

import Matrix.Loader.AbstractMetaData;

public class ImageMetaData extends AbstractMetaData{
	private int width;
	private int height;
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public void setItemsRead(int itemsRead) {
		// TODO Auto-generated method stub
		super.setItemsRead(itemsRead);
		super.setTotalItemsRead(super.getTotalItemsRead() + itemsRead);
	}
}
