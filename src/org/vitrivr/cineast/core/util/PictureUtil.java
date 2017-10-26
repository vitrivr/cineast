package org.vitrivr.cineast.core.util;

import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;

public class PictureUtil {

	private PictureUtil(){}
	
	public static int[] toColorArray(Picture src){
		if (src.getColor() != ColorSpace.RGB) {
            Transform transform = ColorUtil.getTransform(src.getColor(), ColorSpace.RGB);
            Picture rgb = Picture.create(src.getWidth(), src.getHeight(), ColorSpace.RGB, src.getCrop());
            transform.transform(src, rgb);
            src = rgb;
        }
		
		int[] _return = new int[src.getCroppedWidth() * src.getCroppedHeight()];
		
		int[] data = src.getPlaneData(0);
		
		for(int i = 0; i < _return.length; ++i){
			_return[i] = ReadableRGBContainer.toIntColor(data[3*i + 2], data[3*i + 1], data[3*i]);
		}
		
		return _return;
	}
	
}
