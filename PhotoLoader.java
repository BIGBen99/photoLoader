import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

public class PhotoLoader {
	BufferedWriter	logFile;
	File			inputDirectory;
	File			outputDirectory;
	File[]			photos;
	
	public PhotoLoader() throws IOException {
		ImageMetadata		metadata;
		JpegImageMetadata	jpegMetadata;
		Path				source;
		Path				target;
		SimpleDateFormat	sf;
		String				fileMD5;
		String				photoDate;
		String				log;
		String				errorLevel;
		
		logFile = new BufferedWriter(new FileWriter("D:\\Perso\\photoLoader.log", true));
		inputDirectory = new File("D:\\Perso\\PhotoIn\\");
		outputDirectory = new File("D:\\Perso\\PhotoOut\\");
		sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		
		logFile.write(sf.format(new Date()) + "|INFO   |photoLoader v2019.03.28|Démarrage du traitement, source: " + inputDirectory.getPath() + ", cible: " + outputDirectory.getPath());
		logFile.newLine();
		photos = inputDirectory.listFiles();
		logFile.write(sf.format(new Date()) + "|INFO   |photoLoader v2019.03.28|" + photos.length + " élément(s) à traiter");
		logFile.newLine();
		for(int i = 0; i < photos.length; i++) {
			log = "%DATETIME%|%LEVEL%|%FILE%|%MESSAGE%";
			errorLevel = "INFO";
//			System.out.println((i+1) + " / " + photos[i].getName());
//			System.out.println((i+1) + " / " + photos[i].getPath());
			log = log.replaceAll("%FILE%", photos[i].getPath().replaceAll("\\\\", "\\\\\\\\"));
			if(photos[i].isFile()) {
				try {
					fileMD5 = Toolbox.getMD5Checksum(photos[i]);
	//				System.out.println("- MD5: " + fileMD5);
	//				System.out.println("- File.lastModified: " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date(photos[i].lastModified())));
					metadata = Imaging.getMetadata(photos[i]);
					
	//				System.out.println(metadata);
					
					if (metadata instanceof JpegImageMetadata) {
						jpegMetadata = (JpegImageMetadata) metadata;
						
						photoDate = "\\" + getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL).substring(1, 5) + "\\" + getTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL).substring(6, 8) + "\\";
						if(photoDate.equals("\\0000\\00\\")) {
							photoDate = new SimpleDateFormat("'\\'yyyy'\\'MM'\\'").format(new Date(photos[i].lastModified()));
//							System.out.println("- Photo date (Bad EXIF, File.lastModified): " + photoDate);
							errorLevel = "WARNING";
							log = log.replaceAll("%MESSAGE%", "%MESSAGE% La date de prise de vue n'est pas valorisée dans les données EXIF.");
						} else {
//							System.out.println("- Photo date (EXIF): " + photoDate);
						}
					} else {
						photoDate = new SimpleDateFormat("'\\'yyyy'\\'MM'\\'").format(new Date(photos[i].lastModified()));
//						System.out.println("- Photo date (File.lastModified): " + photoDate);
					}
					source = Paths.get(photos[i].getPath());
					target = Paths.get(outputDirectory + photoDate + fileMD5 + ".jpg");
					Files.createDirectories(Paths.get(outputDirectory + photoDate));
//					System.out.println(target);
					try {
						Files.move(source, target);
						errorLevel = "INFO   ";
						log = log.replaceAll("%MESSAGE%", "Fichier traité (destination: " + target.toString().replaceAll("\\\\", "\\\\\\\\") + ").");
					} catch(FileAlreadyExistsException ex) {
//						System.out.println("- Le fichier destination existe déjà, doublon ? (" + photoDate + ")");
						errorLevel = "ERROR  ";
						log = log.replaceAll("%MESSAGE%", "Le fichier destination (" + target.toString().replaceAll("\\\\", "\\\\\\\\") + ") existe déjà. %MESSAGE%");
						target = Paths.get(inputDirectory + "\\errors\\" + photos[i].getName());
						Files.createDirectories(Paths.get(inputDirectory + "\\errors"));
						Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
						log = log.replaceAll("%MESSAGE%", "Le fichier source a été déplacé (" + target.toString().replaceAll("\\\\", "\\\\\\\\") + ").");
					}
	
				} catch (NoSuchAlgorithmException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImageReadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
//				System.out.println("- les dossiers ne sont pas traités");
				errorLevel = "WARNING";
				log = log.replaceAll("%MESSAGE%", "Les dossiers ne sont pas traités");
			}
			log = log.replaceAll("%DATETIME%", sf.format(new Date()));
			log = log.replaceAll("%LEVEL%", errorLevel);
			logFile.write(log);
			logFile.newLine();
//			System.out.println(log);
		}
		logFile.write(sf.format(new Date()) + "|INFO   |photoLoader v2019.03.28|Fin du traitement");
		logFile.newLine();
		logFile.flush();
		logFile.close();
	}
	
	private static void printTagValue(JpegImageMetadata jpegMetadata, TagInfo tagInfo) {
        TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }

	private static String getTagValue(JpegImageMetadata jpegMetadata, TagInfo tagInfo) throws Exception {
        TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            //throws new TagNotFoundException();
            throw new Exception("Tag " + tagInfo.name + " not found.");
        }
        return field.getValueDescription();
    }
	
	public static void main(String[] args) {
		Date				startDate;
		Date				endDate;
		SimpleDateFormat	sf;
		
		sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		startDate = new Date();
		System.out.println(sf.format(startDate) + " START");
		try {
			new PhotoLoader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endDate = new Date();
		System.out.println(sf.format(endDate) + " STOP");
	}
}