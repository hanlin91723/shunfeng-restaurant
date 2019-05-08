package com.wallet.sign;

import com.wallet.sign.CryptoException;

import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;


/**
 * Created by Administrator on 2017/12/20.
 */

public class EncryptedFileUtils {

    /*static PrivateKey getPrivateKeyFromBytes(byte[] data)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        final Reader pemReader = new StringReader(new String(data));
        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }

        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getPrivateKey(pemPair);

        return privateKey;
    }
*/
    static PrivateKey getPrivateKeyFromBytes(String data)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        PEMParser pp = new PEMParser(new StringReader(data));
        PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
        pp.close();
        return new JcaPEMKeyConverter().getKeyPair(pemKeyPair).getPrivate();

    }
    /**
     * Sign data with the specified elliptic curve private key.
     *
     * @param privateKey elliptic curve private key.
     * @param data       data to sign
     * @return the signed data.
     * @throws CryptoException
     */
    public static BigInteger[] ecdsaSignToBytes(ECPrivateKey privateKey, byte[] encoded) throws CryptoException {
        try {


            //final byte[] encoded = hash(data);
            String curveName = "P-256";
            X9ECParameters params = NISTNamedCurves.getByName(curveName);
            BigInteger curveN = params.getN();

            ECDomainParameters ecParams = new ECDomainParameters(params.getCurve(), params.getG(), curveN,
                    params.getH());
            params.getCurve();
            ECDSASigner signer = new ECDSASigner();

            ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey.getS(), ecParams);
            signer.init(true, privKey);
            BigInteger[] sigs = signer.generateSignature(encoded);


            //signer.verifySignature(arg1, arg2, arg2)
            sigs = preventMalleability(sigs, curveN);
//
//            ByteArrayOutputStream s = new ByteArrayOutputStream();
//
//            DERSequenceGenerator seq = new DERSequenceGenerator(s);
//            seq.addObject(new ASN1Integer(sigs[0]));
//            seq.addObject(new ASN1Integer(sigs[1]));
//            seq.close();


            //System.out.println("singed data is " + new String(s.toByteArray()));
            return sigs;

        } catch (Exception e) {
            throw new CryptoException("Could not sign the message using private key", e);
        }

    }

    private static Digest getHashDigest() {
            return new SHA256Digest();
    }
    public static byte[] hash(byte[] input) {
        Digest digest = getHashDigest();
        byte[] retValue = new byte[digest.getDigestSize()];
        digest.update(input, 0, input.length);
        digest.doFinal(retValue, 0);
        return retValue;
    }
    private static BigInteger[] preventMalleability(BigInteger[] sigs, BigInteger curveN) {
        BigInteger cmpVal = curveN.divide(BigInteger.valueOf(2L));

        BigInteger sval = sigs[1];

        if (sval.compareTo(cmpVal) == 1) {

            sigs[1] = curveN.subtract(sval);
        }

        return sigs;
    }

}
