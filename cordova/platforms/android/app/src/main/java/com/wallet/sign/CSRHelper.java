package com.wallet.sign;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.io.pem.PemObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

public class CSRHelper {
	public final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withECDSA";//SHA256withRSA
	  private final static String CN_PATTERN = "CN=%s, OU=IT, EMAILADDRESS=service@xi-tech.com.cn, O=Xiaoxi, ST=重庆, C=CN, L=重庆";
	  private static class JCESigner implements ContentSigner {

	        private static Map<String, AlgorithmIdentifier> ALGOS = new HashMap<String, AlgorithmIdentifier>();

	        static {
	            ALGOS.put("SHA256withRSA".toLowerCase(), new AlgorithmIdentifier(
	                    new ASN1ObjectIdentifier("1.2.840.113549.1.1.11")));
	            ALGOS.put("SHA1withRSA".toLowerCase(), new AlgorithmIdentifier(
	                    new ASN1ObjectIdentifier("1.2.840.113549.1.1.5")));

	        }

	        private String mAlgo;
	        private Signature signature;
	        private ByteArrayOutputStream outputStream;

	        public JCESigner(PrivateKey privateKey, String sigAlgo) {
	            //Utils.throwIfNull(privateKey, sigAlgo);
	            mAlgo = sigAlgo.toLowerCase();
	            try {
	                this.outputStream = new ByteArrayOutputStream();
	                this.signature = Signature.getInstance(sigAlgo);
	                this.signature.initSign(privateKey);
	            } catch (GeneralSecurityException gse) {
	                throw new IllegalArgumentException(gse.getMessage());
	            }
	        }

	        @Override
	        public AlgorithmIdentifier getAlgorithmIdentifier() {
	            AlgorithmIdentifier id = ALGOS.get(mAlgo);
	            if (id == null) {
	                throw new IllegalArgumentException("Does not support algo: " +
	                        mAlgo);
	            }
	            return id;
	        }

	        @Override
	        public OutputStream getOutputStream() {
	            return outputStream;
	        }

	        @Override
	        public byte[] getSignature() {
	            try {
	                signature.update(outputStream.toByteArray());
	                return signature.sign();
	            } catch (GeneralSecurityException gse) {
	                gse.printStackTrace();
	                return null;
	            }
	        }
	    }

	//Create the certificate signing request (CSR) from private and public keys
	public static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String cn) throws IOException,
	            OperatorCreationException {
//	        String principal = String.format(CN_PATTERN, cn);
//
//	        ContentSigner signer = new JCESigner (keyPair.getPrivate(),DEFAULT_SIGNATURE_ALGORITHM);
//
//	        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
//	                new X500Name(principal), keyPair.getPublic());
//	        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
//	        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(
//	                true));
//	        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
//	                extensionsGenerator.generate());
//	        PKCS10CertificationRequest csr = csrBuilder.build(signer);
		String principal = String.format(CN_PATTERN, cn);
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(principal), keyPair.getPublic());
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(DEFAULT_SIGNATURE_ALGORITHM);
		ContentSigner signer = csBuilder.build(keyPair.getPrivate());
	        return p10Builder.build(signer);
	    }
	
	public static String certificationRequestToPEM(PKCS10CertificationRequest csr) throws IOException {
		PemObject pemCSR = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());

		StringWriter str = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(str);
		pemWriter.writeObject(pemCSR);
		pemWriter.close();
		str.close();
		return str.toString();
	}

	public static String privateKeyToPEM(PrivateKey privateKey) throws IOException {
		PemObject pemKey = new PemObject("PRIVATE KEY", privateKey.getEncoded());

		StringWriter str = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(str);
		pemWriter.writeObject(pemKey);
		pemWriter.close();
		str.close();
		return str.toString();
	}

	public static PrivateKey pemToPrivateKey(String pem, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {

		String privateKeyPem = "";
		if(pem.contains("-----BEGIN PRIVATE KEY-----"))
        {
            privateKeyPem = pem.replace("-----BEGIN PRIVATE KEY-----\n","");
            privateKeyPem = privateKeyPem.replace("-----END PRIVATE KEY-----\n","");
        }
        else
        {
            privateKeyPem = pem.replace("-----BEGIN EC PRIVATE KEY-----\n","");
            privateKeyPem = privateKeyPem.replace("-----END EC PRIVATE KEY-----\n","");

            //注意 ，如果是PKCS1 ，而非PCKS8，有问题
        }

		Base64 base64 = new Base64();
		byte[] decoded = base64.decode(privateKeyPem);

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded); //读取的私钥必须是经过PKCS8的

		KeyFactory keyFactory = KeyFactory.getInstance(algorithm,"AndroidOpenSSL");

		return keyFactory.generatePrivate(spec);
	}

	public static String publicKeyToPEM(PublicKey key) throws IOException {
		PemObject pemKey = new PemObject("PUBLIC KEY", key.getEncoded());

		StringWriter str = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(str);
		pemWriter.writeObject(pemKey);
		pemWriter.close();
		str.close();
		return str.toString();
	}


}
