/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.captcha;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CaptchaImage extends Captcha {
    
    static  {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
    }    
    
    private static final Logger LOG = LoggerFactory.getLogger(CaptchaImage.class);
    
    // We can have several images for the same captcha
    byte[][] data;
    byte[][] md5;
    String[] mimes;
    String response;
    
    public CaptchaImage(byte[][] images){
        this.data = images;
        if(images != null && images.length > 0){
            
            md5 = new byte[images.length][];
            mimes = new String[images.length];
            for (int i= 0; i < data.length; i++) {

                try {
                    md5[i] = MessageDigest.getInstance("MD5").digest(data[i]);
                } catch (NoSuchAlgorithmException ex) {
                    LOG.error("can't hash image", ex);
                }
                
                //int loop=0;
                mimes[i] = "";
                Collection<?> mimeTypes = MimeUtil.getMimeTypes(data[i]);
                if(mimeTypes != null && !mimeTypes.isEmpty()){
                    MimeType mType = MimeUtil.getMostSpecificMimeType(mimeTypes);
                    if(mType != null){
                        mimes[i] = mType.toString();
                    }
                }
            }
        }

    }

    public byte[] getImage(){
        return data[0];
    }
    public String getMd5(){
        if(md5 == null || md5.length == 0 || md5[0] == null)
            return null;

        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < md5[0].length; i++) {
            String hex = Integer.toHexString(md5[0][i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }

        return hashString.toString();
    }
    public byte[][] getAllImages(){
        return data;
    }
    public byte[][] getAllMd5(){
        return md5;
    }

    public String[] getMimes() {
        return mimes;
    }    

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
    
}
