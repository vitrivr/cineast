package org.vitrivr.cineast.core.util;

import java.io.File;
import java.io.FileFilter;

public class FileUtil {

	private FileUtil() {
	}

	public static final VideoFileFilter VIDEO_FILE_FILTER = new VideoFileFilter();
	public static final SubtitleFileFilter SUBTITLE_FILE_FILTER = new SubtitleFileFilter();
	
	/**
	 * Checks if a filename looks like one of a video file.
	 * @param fileName
	 * @return true if the filename end in [avi, mp4, mkv, mov, mpg, webm]
	 */
	public static boolean isVideoFileName(String fileName) {
		String lowerCase = fileName.toLowerCase();
		if (lowerCase.endsWith(".avi")) {
			return true;
		}
		if (lowerCase.endsWith(".mp4")) {
			return true;
		}
		if (lowerCase.endsWith(".mkv")) {
			return true;
		}
		if (lowerCase.endsWith(".mov")) {
			return true;
		}
		if (lowerCase.endsWith(".mpg")) {
			return true;
		}
		if (lowerCase.endsWith(".webm")) {
			return true;
		}

		return false;
	}

	/**
	 * 
	 * Filters for video files as defined in {@link FileUtil#isVideoFileName(String)}}
	 *
	 */
	public static class VideoFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			if(!pathname.isFile()){
				return false;
			}
			return isVideoFileName(pathname.getAbsolutePath());
		}
	}

	/**
	 * Checks if a filename looks like one of a subtitle file.
	 * @param fileName
	 * @return true if the filename end in [srt]
	 */
	public static boolean isSubtitleFileName(String fileName) {
		String lowerCase = fileName.toLowerCase();
		if (lowerCase.endsWith(".srt")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * Filters for subtitle files as defined in {@link FileUtil#isSubtitleFileName(String)}}
	 *
	 */
	public static class SubtitleFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			if(!pathname.isFile()){
				return false;
			}
			return isSubtitleFileName(pathname.getAbsolutePath());
		}
	}

}
