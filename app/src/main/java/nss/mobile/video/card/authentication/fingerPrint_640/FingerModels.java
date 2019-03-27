package nss.mobile.video.card.authentication.fingerPrint_640;


import org.litepal.crud.LitePalSupport;

public class FingerModels extends LitePalSupport {
	private String model_ID;
	private byte[] model;

	public String getId() {
		return model_ID;
	}

	public void setId(String id) {
		this.model_ID = id;
	}

	public byte[] getModel() {
		return model;
	}

	public void setModel(byte[] model) {
		this.model = model;
	}
}