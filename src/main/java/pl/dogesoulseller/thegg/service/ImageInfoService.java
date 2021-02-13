package pl.dogesoulseller.thegg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.Utility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.imageio.ImageIO;

@Service
public class ImageInfoService {
	private final HashMap<String, String> MIME_MAP = new HashMap<>(5);

	@Autowired
	public ImageInfoService() {
		// TODO: Support more types
		MIME_MAP.put("image/png", ".png");
		MIME_MAP.put("image/x-png", ".png");
		MIME_MAP.put("image/jpg", ".jpg");
		MIME_MAP.put("image/jpeg", ".jpg");
		MIME_MAP.put("image/gif", ".gif");
	}

	public String getUniqueImageHash(BufferedImage image) {
		// SHA1 is good enough for non-cryptographic uses
		MessageDigest digest;

		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Failed to initialize SHA-1 encoder");
		}

		// Assume width * height * RGBA
		var byteOutputStream = new ByteArrayOutputStream(image.getWidth() * image.getHeight() * 4);
		try {
			ImageIO.write(image, "png", byteOutputStream);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read image bytes");
		}

		byte[] hash = digest.digest(byteOutputStream.toByteArray());

		return Utility.bytesToHexString(hash).toLowerCase();
	}

	public String getMimeExtension(String mime) {
		return MIME_MAP.get(mime);
	}
}