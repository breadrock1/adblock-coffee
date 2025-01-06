use jni::objects::{JList, JObjectArray, JString};
use jni::sys::{jboolean, jint, jlong};
use jni::JNIEnv;
use lazy_static::lazy_static;
use once_cell::sync::Lazy;
use std::collections::HashMap;
use std::sync::Mutex;

use crate::adblock::AdvtBlocker;
use crate::errors::{Result, RustException};

lazy_static! {
    static ref INSTANCE_POOL: Lazy<Mutex<HashMap<jlong, AdvtBlocker>>> =
        Lazy::new(|| { Mutex::new(HashMap::default()) });
}

pub(crate) fn init_object_wrapped(env: &mut JNIEnv, rules: &JObjectArray) -> Result<jlong> {
    let conv_rules = extract_list_str(env, rules)?;

    let advt_instance = AdvtBlocker::new(conv_rules);
    let ptr = Box::into_raw(Box::new(&advt_instance)) as jlong;

    let mut instance_lock = INSTANCE_POOL.lock()?;
    instance_lock.insert(ptr, advt_instance);

    Ok(ptr)
}

pub(crate) fn destroy_object_wrapped(_env: &mut JNIEnv, ptr: jlong) -> Result<jboolean> {
    let mut instance_lock = INSTANCE_POOL.lock()?;

    let Some(instance) = instance_lock.remove(&ptr) else {
        let msg = format!("failed to remove instance: {ptr:?}");
        return Err(RustException::InstanceAccess(msg));
    };

    drop(instance);
    Ok(true as jboolean)
}

pub(crate) fn check_net_urls_wrapped(
    env: &mut JNIEnv,
    ptr: jlong,
    url: &JString,
    src_url: &JString,
    req_type: &JString,
) -> Result<jboolean> {
    let instance_lock = INSTANCE_POOL.lock()?;
    let Some(advt_blocker) = instance_lock.get(&ptr) else {
        let msg = format!("failed to get instance: {ptr:?}");
        return Err(RustException::InstanceAccess(msg));
    };

    let url_str = extract_str(env, url)?;
    let src_url_str = extract_str(env, src_url)?;
    let req_type_str = extract_str(env, req_type)?;

    advt_blocker
        .check_network_urls(&url_str, &src_url_str, &req_type_str)
        .map(|result| result as jboolean)
}

fn extract_list_str<'a>(env: &'a mut JNIEnv, j_obj_arr: &'a JObjectArray) -> Result<Vec<String>> {
    let j_list = env.get_list(j_obj_arr)?;
    let j_list_size = j_list.size(env)?;

    let mut list_data = Vec::with_capacity(j_list_size as usize);
    for index in 0..j_list_size {
        match extract_entity(env, &j_list, index) {
            Ok(data) => list_data.push(data),
            Err(err) => {
                log::error!("failed to extract str from java object: {err:#?}");
                continue;
            }
        }
    }

    Ok(list_data)
}

fn extract_entity(env: &mut JNIEnv, j_list: &JList, index: jint) -> Result<String> {
    let j_obj_opt = j_list.get(env, index)?;
    let j_str = match j_obj_opt {
        Some(j_obj) => JString::from(j_obj),
        None => {
            let msg = format!("parsed rule is none: {j_obj_opt:?}. skipped...");
            return Err(RustException::ParseJavaObject(msg));
        }
    };

    extract_str(env, &j_str)
}

fn extract_str<'a>(env: &'a mut JNIEnv, j_obj: &'a JString) -> Result<String> {
    let j_str = env.get_string(j_obj)?;
    let str_obj = j_str.to_str()?;
    Ok(str_obj.to_string())
}
