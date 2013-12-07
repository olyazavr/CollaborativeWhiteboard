package scribe.services;

import java.io.UnsupportedEncodingException;

import restfb.util.EncodingUtils.Base64;
import scribe.exceptions.OAuthSignatureException;

public class CommonsEncoder extends Base64Encoder
{

  @Override
  public String encode(byte[] bytes)
  {
    try
    {
            return new String(Base64.encodeToByte(bytes, false), "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      throw new OAuthSignatureException("Can't perform base64 encoding", e);
    }
  }

  @Override
  public String getType()
  {
    return "CommonsCodec";
  }

  public static boolean isPresent()
  {
    try
    {
      Class.forName("org.apache.commons.codec.binary.Base64");
      return true;
    }
    catch (ClassNotFoundException e)
    {
      return false;
    }
  }
}
