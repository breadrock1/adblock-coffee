use jni::errors::{Error, Exception, ToException};

use adblock::request::RequestError;
use std::fmt::Debug;
use std::str::Utf8Error;
use std::sync::PoisonError;
use thiserror::Error;

pub(crate) type Result<T> = std::result::Result<T, RustException>;

#[derive(Debug, Error)]
pub enum RustException {
    #[error("failed to create request: {0}")]
    CreateRequest(#[from] RequestError),
    #[error("failed to extract java parameter: {0}")]
    ExtractParameter(#[from] Utf8Error),
    #[error("failed to parse java object: {0}")]
    ParseJavaObject(String),
    #[error("Failed while lock mutex for AdvtBlocker: {0}")]
    InstanceAccess(String),
    #[error("JVM runtime error: {0}")]
    JvmException(String),
}

impl ToException for RustException {
    fn to_exception(&self) -> Exception {
        Exception {
            class: "RustException".to_string(),
            msg: self.to_string(),
        }
    }
}

impl<T> From<PoisonError<T>> for RustException {
    fn from(value: PoisonError<T>) -> Self {
        RustException::InstanceAccess(value.to_string())
    }
}

impl From<jni::errors::Error> for RustException {
    fn from(value: Error) -> Self {
        RustException::JvmException(value.to_string())
    }
}
