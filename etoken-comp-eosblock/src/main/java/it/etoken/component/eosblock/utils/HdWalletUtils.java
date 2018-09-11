package it.etoken.component.eosblock.utils;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.web3j.crypto.Keys;

public class HdWalletUtils {
    //测试链可以用TestNet3Params
    private static final MainNetParams mainnetParams = new MainNetParams();


    /**
     * 生成 BTC 比特币地址
     * @param addressIndex
     * @param ext_key
     * @return
     */
    public static String getBtcAddress(int addressIndex, String ext_key) {
        DeterministicKey parentDK = DeterministicKey.deserializeB58(ext_key, mainnetParams);
        DeterministicKey childDK = HDKeyDerivation.deriveChildKey(parentDK, addressIndex);
        return childDK.toAddress(mainnetParams).toBase58();
    }

    /**
     * 生成 ETH 以太币地址
     * @param addressIndex
     * @param ext_key
     * @return
     */
    public static String getEthAddress(int addressIndex, String ext_key) {
        DeterministicKey parentDK = DeterministicKey.deserializeB58(ext_key, mainnetParams);
        DeterministicKey childDK = HDKeyDerivation.deriveChildKey(parentDK, addressIndex);
        ECKey uncompressedChildKey = childDK.decompress();
        //以太坊需要把前缀去掉（0x04前缀表示未压缩）
        String hexK = uncompressedChildKey.getPublicKeyAsHex().substring(2);
        String addr = Keys.getAddress(hexK);
        return Keys.toChecksumAddress(addr);
    }
    
    public static void main(String[] args) {
    	String ext_key = "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8";
    	String xxx = HdWalletUtils.getEthAddress(0, ext_key);
    	String yyy = HdWalletUtils.getEthAddress(1, ext_key);
    	String aaa = HdWalletUtils.getBtcAddress(0, ext_key);
    	String bbb = HdWalletUtils.getBtcAddress(1, ext_key);
    	System.out.println(xxx);
    	System.out.println(yyy);
    	System.out.println(aaa);
    	System.out.println(bbb);
    }
}