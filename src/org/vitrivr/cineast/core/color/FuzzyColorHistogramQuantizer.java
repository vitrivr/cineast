package org.vitrivr.cineast.core.color;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* based on Article "Fuzzy color histogram-based video segmentation" by Onur K���ktun�, Ugur G�d�kbay, �zg�r Ulusoy */
public class FuzzyColorHistogramQuantizer {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private FuzzyColorHistogramQuantizer(){}
	
	public static enum Color{
		Black, Blue, Navy, Red, Yellow, Magenta, Brown, Grey, Green, Teal, Violet, Orange, Pink, White, Cyan
	};
	
	static boolean isBlack(ReadableLabContainer lab){
		return lab.L < 40f;
	}
	
	static boolean isGrey(ReadableLabContainer lab){
		return lab.L > 10f && lab.L < 90f;
	}
	
	static boolean isWhite(ReadableLabContainer lab){
		return lab.L > 60f;
	}
	
	static boolean isGreen(ReadableLabContainer lab){
		return lab.a < -30f;
	}
	
	static boolean isGreenish(ReadableLabContainer lab){
		return lab.a > -70f && lab.a < 0f;
	}
	
	static boolean isAmiddle(ReadableLabContainer lab){
		return lab.a > -12f && lab.a < 12f;
	}
	
	static boolean isReddish(ReadableLabContainer lab){
		return lab.a > 0f && lab.a < 80f;
	}
	
	static boolean isRed(ReadableLabContainer lab){
		return lab.a > 40f;
	}
	
	static boolean isBlue(ReadableLabContainer lab){
		return lab.b < -50f;
	}
	
	static boolean isBluish(ReadableLabContainer lab){
		return lab.b > -90f && lab.b < 0f;
	}
	
	static boolean isBmiddle(ReadableLabContainer lab){
		return lab.b > -12f && lab.b < 12f;
	}
	
	static boolean isYellowish(ReadableLabContainer lab){
		return lab.b > 0f && lab.b < 75f;
	}
	
	static boolean isYellow(ReadableLabContainer lab){
		return lab.b > 35f;
	}
	
	public static Color quantize(ReadableLabContainer lab){
		/*if(isBlack(lab)	&& isAmiddle(lab)	&& isBmiddle(lab))	{return Color.Black;}
		if(isBlack(lab) 					&& isBluish(lab))	{return Color.Blue;}
		if(isGrey(lab)	&& !isGreen(lab) 	&& isBlue(lab))		{return Color.Blue;}
		if(isWhite(lab) && isAmiddle(lab) 	&& isBluish(lab))	{return Color.Blue;}
		if(isWhite(lab) && isGreenish(lab) 	&& isBluish(lab))	{return Color.Blue;}
		if(isBlack(lab) && isReddish(lab) 	&& isBlue(lab))		{return Color.Navy;}
		if(isGrey(lab)	&& isRed(lab) 		&& !isBlue(lab))	{return Color.Red;}
		if(isGrey(lab)	&& isReddish(lab) 	&& isBmiddle(lab))	{return Color.Red;}
		if(isBlack(lab) && isReddish(lab) 	&& isYellowish(lab)){return Color.Red;}
		if(isGrey(lab)	&& isReddish(lab) 	&& isYellow(lab))	{return Color.Yellow;}
		if(isWhite(lab) && isAmiddle(lab) 	&& isYellow(lab))	{return Color.Yellow;}
		if(isWhite(lab) && isGreenish(lab) 	&& isYellow(lab))	{return Color.Yellow;}
		if(isGrey(lab)	&& isReddish(lab) 	&& isBluish(lab))	{return Color.Magenta;}
		if(isWhite(lab) && isReddish(lab) 	&& isBluish(lab))	{return Color.Magenta;}
		if(isGrey(lab)	&& isAmiddle(lab) 	&& isYellowish(lab)){return Color.Brown;}
		if(isWhite(lab) && isReddish(lab) 	&& isYellow(lab))	{return Color.Brown;}
		if(isGrey(lab)	&& isAmiddle(lab) 	&& isBmiddle(lab))	{return Color.Grey;}
		if(isGrey(lab) 	&& isGreenish(lab) 	&& isYellow(lab))	{return Color.Green;}
		if(isWhite(lab) && isGreen(lab) 	&& isYellowish(lab)){return Color.Green;}
		if(isGrey(lab)	&& isGreenish(lab) 	&& isBmiddle(lab))	{return Color.Teal;}
		if(isGrey(lab) 	&& isGreen(lab) 	&& isBlue(lab))		{return Color.Violet;}
		if(isWhite(lab) && isRed(lab) 		&& isYellow(lab))	{return Color.Orange;}
		if(isWhite(lab) && isReddish(lab) 	&& isBmiddle(lab))	{return Color.Pink;}
		if(isWhite(lab) && isAmiddle(lab) 	&& isBmiddle(lab))	{return Color.White;}
		if(isWhite(lab) && isGreenish(lab) 	&& isBmiddle(lab))	{return Color.Cyan;}
		if(isWhite(lab) && isGreen(lab) 	&& isBluish(lab))	{return Color.Cyan;}*/
		
		//added rules to close gaps
		//if(isGrey(lab))											{return Color.Grey;}
		//if(isWhite(lab))										{return Color.White;}
		//if(isBlack(lab))										{return Color.Black;}
		
		if(isWhite(lab)){
			
			if(isAmiddle(lab)){
				if(isBmiddle(lab)){
					return Color.White;
				}
				if(isYellowish(lab) || isYellow(lab)){
					return Color.Yellow;
				}
				return Color.Blue;
			}
			
			if(isReddish(lab)){
				if(isBluish(lab)){
					return Color.Magenta;
				}
				if(isYellow(lab)){
					return Color.Brown;
				}
				if(isBmiddle(lab)){
					return Color.Pink;
				}
			}
			
			if(isGreenish(lab)){
				if(isBmiddle(lab)){
					return Color.Cyan;
				}
				if(isBluish(lab)){
					return Color.Blue;
				}
				if(isYellowish(lab)){
					return Color.Green;
				}
				if(isYellow(lab)){
					return Color.Yellow;
				}
			}
			
			if(isRed(lab)){
				if(isYellow(lab)){
					return Color.Orange;
				}
			}
			
			if(isGreen(lab)){
				if(isBlue(lab) || isBluish(lab)){
					return Color.Cyan;
				}
				if(isYellow(lab) || isYellowish(lab)){
					return Color.Green;
				}
			}
			
		}
		
		if(isBlack(lab)){
			if(isAmiddle(lab)){
				if(isBmiddle(lab)){
					return Color.Black;
				}
				if(isBlue(lab) || isBluish(lab)){
					return Color.Blue;
				}
			}
			if(isReddish(lab)){
				if(isBluish(lab)){
					return Color.Blue;
				}
				if(isYellowish(lab)){
					return Color.Red;
				}
				if(isBlue(lab)){
					return Color.Navy;
				}
			}
			if(isGreenish(lab)){
				if(isBluish(lab)){
					return Color.Blue;
				}
				if(isYellowish(lab)){
					return Color.Teal;
				}
			}
			if(isRed(lab)){
				if(isBluish(lab)){
					return Color.Blue;
				}
			}
			if(isGreen(lab)){
				if(isBluish(lab)){
					return Color.Blue;
				}
			}
		}
		
		
		if(isGrey(lab)){
			if(isAmiddle(lab)){
				if(isBmiddle(lab)){
					return Color.Grey;
				}
				if(isBlue(lab)){
					return Color.Blue;
				}
				if(isYellowish(lab)){
					return Color.Brown;
				}
			}
			if(isReddish(lab)){
				if(isBmiddle(lab)){
					return Color.Red;
				}
				if(isBluish(lab)){
					return Color.Magenta;
				}
				if(isYellowish(lab)){
					return Color.Orange;
				}
				if(isBlue(lab)){
					return Color.Blue;
				}
				if(isYellow(lab)){
					return Color.Yellow;
				}
			}
			if(isGreenish(lab)){
				if(isBmiddle(lab)){
					return Color.Teal;
				}
				if(isBlue(lab)){
					return Color.Blue;
				}
				if(isYellowish(lab)){
					return Color.Green;
				}
				if(isBluish(lab)){
					return Color.Teal;
				}
			}
			if(isRed(lab)){
				if(isBlue(lab)){
					return Color.Blue;
				}
				return Color.Red;
			}
			if(isGreen(lab)){
				if(isBlue(lab)){
					return Color.Violet;
				}
				if(isYellow(lab)){
					return Color.Green;
				}
			}
		}
		
		LOGGER.warn("Error while quantizing {} returning Black", lab);
		return Color.Black;
	}
	
	public static RGBContainer toRGB(Color col){
		switch(col){
		case Black:
			return new RGBContainer(0, 0, 0);
		case Blue:
			return new RGBContainer(0, 0, 255);
		case Brown:
			return new RGBContainer(150, 75, 0);
		case Cyan:
			return new RGBContainer(0, 255, 255);
		case Green:
			return new RGBContainer(0, 255, 0);
		case Grey:
			return new RGBContainer(128, 128, 128);
		case Magenta:
			return new RGBContainer(255, 0, 255);
		case Navy:
			return new RGBContainer(0, 0, 128);
		case Orange:
			return new RGBContainer(255, 127, 0);
		case Pink:
			return new RGBContainer(255, 192, 203);
		case Red:
			return new RGBContainer(255, 0, 0);
		case Teal:
			return new RGBContainer(0, 128, 128);
		case Violet:
			return new RGBContainer(143, 0, 255);
		case White:
			return new RGBContainer(255, 255, 255);
		case Yellow:
			return new RGBContainer(255, 255, 0);
		default:
			return new RGBContainer(0, 0, 0);
		
		}
	}
}
