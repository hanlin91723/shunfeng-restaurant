package com.wallet.sign;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.io.pem.PemObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

public class CSRHelper {
	private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";
	  private final static String CN_PATTERN = "CN=%s, OU=OU, EMAILADDRESS=123@qq.com, O=example.com, ST=beijing, C=CN, L=beijing";
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
	        String principal = String.format(CN_PATTERN, cn);

	        ContentSigner signer = new JCESigner (keyPair.getPrivate(),DEFAULT_SIGNATURE_ALGORITHM);

	        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
	                new X500Name(principal), keyPair.getPublic());
	        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
	        extensionsGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(
	                true));
	        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
	                extensionsGenerator.generate());
	        PKCS10CertificationRequest csr = csrBuilder.build(signer);

	        return csr;
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


}
