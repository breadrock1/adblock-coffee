package com.example.adblock;

import java.util.List;

import com.example.adblock.exception.RustException;

/**
 * This public wrapper class provides base functionality of adblock-rust library.
 * First of all you have to create class instance by {@link #createInstance(List<String>)
 * AdvtBlocker} method passing {@link List} of combined rules. After that you may invoke
 * {@link #checkUrls(String url, String sourceUrl, String requestType) AdvtBlocker} method passing
 * URL, source URL and request type string parameters to validate URL address.
 */
public class AdvtBlocker {
    /**
     * This static code block loading external library to common address space.
     * It's allow to invoke all defined external methods by ffi.
     */
    static {
        LoadLibraryHelper.loadNativeLibrary();
    }

    /**
     * Pointer of initialized AdvtBlocker struct into external library.
     */
    private long advtBlockerPtr;

    /**
     * There is hidden base constructor. This decision was made because initialization of
     * AdvtBlocker struct of external library may throw exception. Also need passing initialized
     * external object pointer which is not a clean way to design library.
     *
     * @param extObjPointer A pointer to external library object AdvtBlocker.
     */
    private AdvtBlocker(long extObjPointer) {
        this.advtBlockerPtr = extObjPointer;
    }

    /**
     * There is simple fabric method to create current class instance with initialized
     * external AdvtBlocker object.
     *
     * @param rules          A list of rules to further URL validation.
     * @return AdvtBlocker   A current class {@link AdvtBlocker} instance.
     * @throws RustException runtime exception from external library.
     */
    public static AdvtBlocker createInstance(List<String> rules) throws RustException {
        long ptr = initObject(rules);
        if (ptr < 0) {
            throw new RustException("failed to initialize AdvtBlocker into rust");
        }

        return new AdvtBlocker(ptr);
    }

    /**
     * This method drops initialized native library object from memory.
     *
     * @throws RustException runtime exception of destroying rust AdvtBlocker object.
     */
    public void destroyInstance() throws RustException {
        if (!destroyObject(this.advtBlockerPtr)) {
            throw new RustException("Failed to destroy AdvtBlocker native library object!");
        }
    }

    /**
     * This method validate passed URL by previously loaded rules.
     *
     * @param url            An URL address to validate.
     * @param sourceUrl      A source URL to extract domain.
     * @param requestType    A request type to validate.
     * @return boolean       A result to validation
     * @throws RustException runtime exception from external library.
     */
    public boolean checkUrls(String url, String sourceUrl, String requestType) throws RustException {
        return checkNetworkUrls(this.advtBlockerPtr, url, sourceUrl, requestType);
    }

    /**
     * This getter method returns current instance address of rust AdvtBlocker object.
     *
     * @return long type address of rust AdvtBlocker object.
     */
    protected long getInstanceAddress() {
        return this.advtBlockerPtr;
    }

    /**
     * There is static native method which defined to get access to external library. Current method
     * initialize external library AdvtBlocker structure with passed rules list and returns pointer
     * to initialized object.
     *
     * @param rules          A list of rules to further URL validation.
     * @throws RustException runtime exception from external library.
     */
    public static native long initObject(List<String> rules) throws RustException;

    /**
     * There is static native method which drops initialized native library object.
     *
     * @param ptr A pointer to external library AdvtBlocker structure to drop.
     * @return boolean status to destroying rust AdvtBlocker object.
     */
    private static native boolean destroyObject(long ptr) throws RustException;

    /**
     * There is native method which defined to get access to external library. Current method
     * invokes external library method to validate passed URL.
     *
     * @param ptr            A pointer to external library AdvtBlocker structure to validate URL.
     * @param url            An URL address to validate.
     * @param sourceUrl      A source URL to extract domain.
     * @param requestType    A request type to validate.
     * @return boolean       A result to validation
     * @throws RustException runtime exception from external library.
     */
    public native boolean checkNetworkUrls(long ptr, String url, String sourceUrl, String requestType) throws RustException;
}
