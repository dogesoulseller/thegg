package pl.dogesoulseller.thegg.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
	private String fileUploadDestination;
	private String imageStorageDir;

	public String getFileUploadDestination() {
		return fileUploadDestination;
	}

	public void setFileUploadDestination(String fileUploadDestination) {
		this.fileUploadDestination = fileUploadDestination;
	}

	public String getImageStorageDir() {
		return imageStorageDir;
	}

	public void setImageStorageDir(String imageStorageDir) {
		this.imageStorageDir = imageStorageDir;
	}
}
