package com.gitelliq.gqhc.captcha;

/*
 * Copyright 2011 Kiraly Attila
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.util.Locale;
import java.util.Random;

import com.github.cage.Cage;
import com.github.cage.image.ConstantColorGenerator;
import com.github.cage.image.EffectConfig;
import com.github.cage.image.Painter;
import com.github.cage.image.ScaleConfig;
import com.github.cage.token.RandomCharacterGeneratorFactory;
import com.github.cage.token.RandomTokenGenerator;

/**
 * Creates and configures a {@link Cage} instance that can generate captcha
 * images similar to Yahoo's. This is the "Y" template. Simply create an
 * instance with <code>new YCage()</code> and you can generate images. See
 * {@link Cage} for more info.
 * 
 * This class is thread safe.
 * 
 * @author akiraly
 */
public class CustomCage extends com.github.cage.Cage {
	/**
	 * Height of CAPTCHA image.
	 */
	protected static final int HEIGHT = 80;

	/**
	 * Width of CAPTCHA image.
	 */
	protected static final int WIDTH = 150;

	/**
	 * Character set supplied to the {@link RandomTokenGenerator} used by this
	 * template.
	 */
//	protected static final char[] TOKEN_DEFAULT_CHARACTER_SET = (new String(
//			RandomCharacterGeneratorFactory.DEFAULT_DEFAULT_CHARACTER_SET)
//			.replaceAll("b|f|i|j|l|m|o|t", "")
//			+ new String(
//					RandomCharacterGeneratorFactory.DEFAULT_DEFAULT_CHARACTER_SET)
//					.replaceAll("c|i|o", "").toUpperCase(Locale.ENGLISH) + new String(
//			RandomCharacterGeneratorFactory.ARABIC_NUMERALS).replaceAll(
//			"0|1|9", "")).toCharArray();

	
	protected static final char[] TOKEN_DEFAULT_CHARACTER_SET = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/**
	 * Minimum length of token.
	 */
	protected static final int TOKEN_LEN_MIN = 4;

	/**
	 * Maximum length of token is {@value #TOKEN_LEN_MIN} +
	 * {@value #TOKEN_LEN_DELTA}.
	 */
	protected static final int TOKEN_LEN_DELTA = 0;

	/**
	 * Constructor.
	 */
	public CustomCage() {
		this(new Random());
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            object used for random value generation. Not null.
	 */
	protected CustomCage(Random rnd) {
		super(new Painter(WIDTH, HEIGHT, Color.BLACK, null, new EffectConfig(true,
				true, true, false, new ScaleConfig(0.55f, 0.55f)), rnd), null,
				new ConstantColorGenerator(Color.WHITE), null,
				Cage.DEFAULT_COMPRESS_RATIO, new RandomTokenGenerator(rnd,
						new RandomCharacterGeneratorFactory(
								TOKEN_DEFAULT_CHARACTER_SET, null, rnd),
						TOKEN_LEN_MIN, TOKEN_LEN_DELTA), rnd);
	}
}
