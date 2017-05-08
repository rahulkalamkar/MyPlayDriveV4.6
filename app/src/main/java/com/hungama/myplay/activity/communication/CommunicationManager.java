package com.hungama.myplay.activity.communication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.exceptions.ContentNotAvailableException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.communication.exceptions.RecreateLoginException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.SessionCreateOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.util.LogUtil;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Manages Communication operations with the applications services. Currently
 * now, it can performs only single task per manager's instance.
 */
public class CommunicationManager implements Callable {

    private static final String TAG = "CommunicationManager";

    public static final int RESPONSE_CONTENT_NOT_MODIFIED_304 = 304;
    public static final int RESPONSE_SERVER_ERROR_500 = 500;
    public static final int RESPONSE_SUCCESS_200 = 200;//
    public static final int RESPONSE_BAD_REQUEST_400 = 400;
    public static final int RESPONSE_FORBIDDEN_403 = 403;
    public static final int RESPONSE_NO_CONTENT_204 = 204;
    // public static final int RESPONSE_NO_INTERNET_CONNECTION_204 = 204;

    public static final String SECRET_KEY = "#HUNGAMA%WS2$15";
    public static final String SECRET_KEY_PAY = "PAYMUSICHUNGAMA#$2015";//"#PAYMUSICHUNGAMA#$2015";

    /**
     * Enumeration definitions for errors that may occurred during the process
     * of the operations.
     */
    public enum ErrorType implements Serializable {

        /**
         * Error which indicates that there is no connection to Internet or has
         * been timeout.
         */
        NO_CONNECTIVITY,

        /**
         * Error which indicates that the given parameters to the request's
         * service are invalid.
         */
        INVALID_REQUEST_PARAMETERS,

        /**
         * Error which indicates that the given token given to request is
         * invalid.
         */
        EXPIRED_REQUEST_TOKEN,

        /**
         * Error which indicates that the retrieved server response is invalid
         * OR the communication protocols among the client / server was broken.
         */
        INTERNAL_SERVER_APPLICATION_ERROR,

        /**
         * Error which indicates that the whole operation was cancelled.
         */
        OPERATION_CANCELLED,

        CONTENT_NOT_AVAILABLE;

    }

    // public static final String ENCODEING_FORMAT_UTF_8 = "UTF-8";

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private static final String PROTOCOL_HTTP = "HTTP";
    private static final String PROTOCOL_HTTPS = "HTTPS";

    private static final String REQUEST_PROPERTY_CONTENT_TYPE_KEY = "Content-Type";
    private static final String REQUEST_PROPERTY_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=UTF-8";

    // public static final int CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS = 45000;
    public static final int CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS = 15;

    // private static final String CONNECTION_PROPERTY = "connection";
    // private static final String CONNECTIOaN_VALUE = "close";

    // private volatile boolean mIsRunning = false;

    public CommunicationManager() {
        //mASyncLock = new ReentrantLock();
    }

    // ======================================================
    // PUBLIC.
    // ======================================================

    // /**
    // * Determines whatever any operation is running.
    // */
    public boolean isRunning() {
        return ((running_Processes != null && running_Processes.size() > 0) || processesQueue != null
                && processesQueue.size() > 0);
    }

    /**
     * Cancel any running operation's progress.</br> In bottom line, interrupts
     * any running worker that performs operations.
     */
    public void cancelAnyRunningOperation() {
        try {

            if (running_Processes != null) {
                processesQueue.clear();

                for (OperationTask process : running_Processes) {
                    try {
                        process.cancel(true);
                    } catch (Exception e) {
                    }
                }
                running_Processes.clear();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        // if (mOperationWorker != null && mOperationWorker.isAlive()) {
        // mOperationWorker.interrupt();
        // }
    }


    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();//8;
    //public static Object sync = new Object();
    //public static ReentrantLock mASyncLock = new ReentrantLock();
    private ThreadPoolManager mPool;

    static Vector<OperationTask> running_Processes = new Vector<CommunicationManager.OperationTask>();
    static MyQueue processesQueue = new MyQueue();

    // <Operation<Operation ;
    /**
     * Performs a communication operation asynchronously.
     *
     //* @param operation
     * to perform.
     //* @param listener
     * for retrieving process events.
     */

    //private CommunicationOperation operation;
    //private CommunicationOperationListener listener;
    //private Context context;
    //private OperationHandler operationHandler;

    class temp extends Thread
    {

    }

    public void performOperationAsync(final CommunicationOperation operation,
                                      CommunicationOperationListener listener, final Context context) {
        //mASyncLock.lock();

        if (running_Processes == null)
            running_Processes = new Vector<CommunicationManager.OperationTask>();

        if (processesQueue == null)
            processesQueue = new MyQueue();

        /*this.operation = operation;
        this.listener = listener;
        this.context = context;*/

        final OperationHandler operationHandler = new OperationHandler(
                listener);
        mPool = ThreadPoolManager.getInstance();//mPool.submit(new Callable<Object>())
        mPool.submit(new Runnable(){

            @Override
            public void run() {
                try {
                    OperationTask task = new OperationTask(operation, operationHandler, context);
//                    processesQueue.add(task);
                    Logger.i("running_Processes**", "Call():0");
//                    if (running_Processes.size() < NUMBER_OF_CORES) {
//                        task = processesQueue.poll();
//                        running_Processes.add(task);
                        task.run();
                        Logger.i("running_Processes**", "Call():1");
//                    }
                    Logger.i("running_Processes**", "Call():2");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //mASyncLock.unlock();
            }
        });

        /*OperationTask task = new OperationTask(operation, new OperationHandler(
                listener), context);
		processesQueue.add(task);
		try {
			if (running_Processes.size() < NUMBER_OF_CORES) {
				task = processesQueue.poll();
				task.start();
				running_Processes.add(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/

        Logger.e("running_Processes**", "" + running_Processes.size());

    }

    @Override
    public Object call() throws Exception {
       /* try {
            OperationTask task = new OperationTask(operation, operationHandler, context);
            processesQueue.add(task);
            Logger.i("running_Processes**", "Call():0");
            if (running_Processes.size() < NUMBER_OF_CORES) {
                task = processesQueue.poll();
                running_Processes.add(task);
                task.run();
                Logger.i("running_Processes**", "Call():1");
            }
            Logger.i("running_Processes**", "Call():2");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //mASyncLock.unlock();
        return null;
    }


    public Map<String, Object> performOperation(
            CommunicationOperation operation, final Context context)
            throws InvalidRequestException, InvalidResponseDataException,
            OperationCancelledException, NoConnectivityException {
        sessionValidation(operation, context);
        String url = operation.getServiceUrl(context);
        Logger.i(TAG, "performOperation req " + url);
        RequestMethod requestMethod = operation.getRequestMethod();
        String requestBody = operation.getRequestBody();
        Logger.i(TAG, "performOperation reqBody " + url);
        String timestamp_cache = operation.getTimeStampCache();
        Response response = null;

        try {
            try {
                response = performRequest(url, requestMethod, requestBody,
                        context, timestamp_cache, operation);
                Logger.i(TAG, "performOperation response " + response);

            } catch (SocketException exception1) {
                Logger.e(TAG,
                        "The connection was reseted... try for the second time.");
                try {
                    response = performRequest(url, requestMethod, requestBody,
                            context, timestamp_cache, operation);
                } catch (SocketException exception2) {
                    Logger.e(TAG,
                            "The connection was reseted... try fo the third time.");
                    response = performRequest(url, requestMethod, requestBody,
                            context, timestamp_cache, operation);
                }
            }

        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw new NoConnectivityException();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new InvalidRequestException();

        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new InvalidRequestException();

        } catch (IOException e) {
            e.printStackTrace();
            throw new NoConnectivityException();

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new OperationCancelledException();
        }

        String temp[] = url.split("/");
        // Logger.i(TAG , temp[temp.length-1] + " response " + response);
        // Runtime.getRuntime().gc();
        LogUtil.splitAndLog(TAG + "Respo", temp[temp.length - 1] + " response "
                + response);
        Map<String, Object> responseData = null;
        try {
            responseData = operation.parseResponse(response);
        } catch (RecreateLoginException e) {
            e.printStackTrace();
        } catch (InvalidResponseDataException e) {
            e.printStackTrace();
        } catch (ContentNotAvailableException e) {
            e.printStackTrace();
        }
        // Runtime.getRuntime().gc();
        return responseData;
    }

    public Response performOperationNew(CommunicationOperation operation,
                                        final Context context) throws InvalidRequestException,
            InvalidResponseDataException, OperationCancelledException,
            NoConnectivityException {
        sessionValidation(operation, context);
        String url = operation.getServiceUrl(context);
        Logger.i(TAG, "performOperation req " + url);
        RequestMethod requestMethod = operation.getRequestMethod();
        String requestBody = operation.getRequestBody();
        String timestamp_cache = operation.getTimeStampCache();
        Response response = null;

        try {
            try {
                response = performRequest(url, requestMethod, requestBody,
                        context, timestamp_cache, operation);
                Logger.i(TAG, "performOperation response " + response);

            } catch (SocketException exception1) {
                Logger.e(TAG,
                        "The connection was reseted... try for the second time.");
                try {
                    response = performRequest(url, requestMethod, requestBody,
                            context, timestamp_cache, operation);
                } catch (SocketException exception2) {
                    Logger.e(TAG,
                            "The connection was reseted... try fo the third time.");
                    response = performRequest(url, requestMethod, requestBody,
                            context, timestamp_cache, operation);
                }
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw new NoConnectivityException();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new InvalidRequestException();
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new InvalidRequestException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NoConnectivityException();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new OperationCancelledException();
        }
        String temp[] = url.split("/");
        // Logger.i(TAG , temp[temp.length-1] + " response " + response);

        LogUtil.splitAndLog(TAG + "Respo", temp[temp.length - 1] + " response "
                + response);
        // Map<String, Object> responseData = operation.parseResponse(response);
        return response;
    }

    // ======================================================
    // BACKROUND PROCESSING MEMBERS.
    // ======================================================

    private static final int MESSAGE_OPERATION_START = 1;
    private static final int MESSAGE_OPERATION_SECCESS = 2;
    private static final int MESSAGE_OPERATION_FAIL = 3;

    private static final String MESSAGE_DATA_KEY_RESPONSE = "message_data_key_response";
    private static final String MESSAGE_DATA_KEY_ERROR_DESCRIPTION = "message_data_key_error_description";
    private static final String MESSAGE_DATA_KEY_ERROR_TYPE = "message_data_key_error_type";

    /**
     * Handler receiving messages of the execution process.
     */
    private static class OperationHandler extends Handler {

        private CommunicationOperationListener mOnCommunicationOperationListener;

        public OperationHandler(
                CommunicationOperationListener onCommunicationOperationListener) {

            mOnCommunicationOperationListener = onCommunicationOperationListener;
        }

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {

                case MESSAGE_OPERATION_START:

                    if (mOnCommunicationOperationListener != null) {
                        mOnCommunicationOperationListener.onStart(message.arg1);
                    }

                    break;

                case MESSAGE_OPERATION_SECCESS:

                    Bundle seccussData = message.getData();
                    HashMap<String, Object> responseData = (HashMap<String, Object>) seccussData
                            .getSerializable(MESSAGE_DATA_KEY_RESPONSE);

                    if (mOnCommunicationOperationListener != null) {

                        if (responseData == null) {
                            responseData = new HashMap<String, Object>();
                        }
                        mOnCommunicationOperationListener.onSuccess(message.arg1,
                                responseData);
                    }

                    break;

                case MESSAGE_OPERATION_FAIL:

                    Bundle failData = message.getData();

                    ErrorType errorType = (ErrorType) failData
                            .getSerializable(MESSAGE_DATA_KEY_ERROR_TYPE);
                    String errorMessage = failData
                            .getString(MESSAGE_DATA_KEY_ERROR_DESCRIPTION);

                    if (mOnCommunicationOperationListener != null) {
                        mOnCommunicationOperationListener.onFailure(message.arg1,
                                errorType, errorMessage);
                    }
                    break;
            }
        }
    }


    private class OperationTask {
        public void run() {
			/*android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);*/
            doInBackground();
        }

        boolean isCancellled = false;

        public void cancel(boolean b) {
            isCancellled = b;

        }

        private CommunicationOperation mCommunicationOperation;
        private OperationHandler mOperationHandler;
        private final Context context;

        public OperationTask(CommunicationOperation communicationOperation,
                             OperationHandler operationHandler, final Context context) {
            mCommunicationOperation = communicationOperation;
            mOperationHandler = operationHandler;
            this.context = context;
        }

        /**
         * Notifies the operation's handler that an error has occurred.
         */
        private void sendErrorMessageToHanlder(int operationId,
                                               ErrorType errorType, String errorMessage) {
            Message message = Message.obtain();
            message.what = MESSAGE_OPERATION_FAIL;
            message.arg1 = operationId;

            Bundle data = new Bundle();
            data.putSerializable(MESSAGE_DATA_KEY_ERROR_TYPE, errorType);
            data.putString(MESSAGE_DATA_KEY_ERROR_DESCRIPTION, errorMessage);

            message.setData(data);
            mOperationHandler.sendMessage(message);
        }

        Message sucessMessage;

        protected Boolean doInBackground() {
            try {
                sessionValidation(mCommunicationOperation, context);
                // notifying for starting the operation.
                Message startMessage = Message.obtain();
                startMessage.arg1 = mCommunicationOperation.getOperationId();
                startMessage.what = MESSAGE_OPERATION_START;
                mOperationHandler.sendMessage(startMessage);
                String url = mCommunicationOperation.getServiceUrl(context);
                RequestMethod requestMethod = mCommunicationOperation
                        .getRequestMethod();
                String requestBody = mCommunicationOperation.getRequestBody();
                String timestamp_cache = mCommunicationOperation
                        .getTimeStampCache();
                Response response = null;
                try {
                    response = performRequest(url, requestMethod, requestBody,
                            context, timestamp_cache, mCommunicationOperation);
                } catch (SocketException exception1) {
                    Logger.e(TAG,
                            "The connection was reseted... try for the second time.");
                    try {
                        response = performRequest(url, requestMethod,
                                requestBody, context, timestamp_cache, mCommunicationOperation);
                    } catch (SocketException exception2) {
                        Logger.e(TAG,
                                "The connection was reseted... try fo the third time.");
                        response = performRequest(url, requestMethod,
                                requestBody, context, timestamp_cache, mCommunicationOperation);
                    }
                }

                if (Logger.isDebuggable) {
                    String temp[] = url.split("/");
                    LogUtil.splitAndLog(TAG, temp[temp.length - 1] + " response "
                            + response);
                }

                Map<String, Object> responseData = mCommunicationOperation
                        .parseResponse(response);
                response = null;
                // notifying for finishing the operation.
                sucessMessage = Message.obtain();
                sucessMessage.arg1 = mCommunicationOperation.getOperationId();
                sucessMessage.what = MESSAGE_OPERATION_SECCESS;
                Bundle data = new Bundle();
                data.putSerializable(MESSAGE_DATA_KEY_RESPONSE,
                        (Serializable) responseData);
                sucessMessage.setData(data);
                onPostExecute(true);
            } catch (OperationCancelledException exception) {
                // operation has been cancelled.
                exception.printStackTrace();
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.OPERATION_CANCELLED,
                        "Operation has been cancelled.");

            } catch (InterruptedException exception) {
                // operation has been cancelled.
                exception.printStackTrace();
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.OPERATION_CANCELLED,
                        "Operation has been cancelled.");

            } catch (MalformedURLException exception) {
                // Bad Url.
                exception.printStackTrace();
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
                        "Invalid service URL.");

            } catch (SocketTimeoutException exception) {
                // timeout.
                exception.printStackTrace();
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.NO_CONNECTIVITY, "No Internet Connection");

            } catch (IOException exception) {
                // File cannot be accessed or is corrupted..
                exception.printStackTrace();
                Logger.e(TAG, "No Internet Connection");
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.NO_CONNECTIVITY, "No Internet Connection");
            } catch (InvalidResponseDataException exception) {
                // Bad data from servers.
                Logger.e(TAG, "Bad response data from servers.");
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
                        exception.getMessage());
            } catch (InvalidRequestParametersException exception) {
                // Bad request parameters.
                Logger.e(
                        TAG,
                        "Bad request parameters. "
                                + Integer.toString(exception.getCode()) + " "
                                + exception.getMessage());
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.INVALID_REQUEST_PARAMETERS,
                        exception.getMessage());

            } catch (InvalidRequestTokenException exception) {
                // Invalid request token.
                Logger.e(TAG, "Invalid request token.");
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.EXPIRED_REQUEST_TOKEN,
                        "Invalid request token.");
            } catch (RecreateLoginException e) {
                Logger.printStackTrace(e);
                // ApplicationConfigurations appConfig = new
                // ApplicationConfigurations(context);
                // appConfig.setSessionID(null);
                // appConfig.setPasskey(null);
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.EXPIRED_REQUEST_TOKEN,
                        "Invalid request token.");
            } catch (ContentNotAvailableException e) {
                Logger.e(TAG, "Bad response data from servers. : ContentNotAvailableException");
                sendErrorMessageToHanlder(
                        mCommunicationOperation.getOperationId(),
                        ErrorType.CONTENT_NOT_AVAILABLE,
                        e.getMessage());
            }
            onPostExecute(false);
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                if (!isCancellled)
                    mOperationHandler.sendMessage(sucessMessage);
            }
            if (running_Processes != null && running_Processes.contains(this))
                running_Processes.remove(this);

			/*OperationTask task = processesQueue.poll();
			if (task != null && running_Processes.size() < NUMBER_OF_CORES)
				task.start();*/
            // if(running_Processes)

        }

    }

    /**
     * Background task for performing communication operations asynchronously.
     */
	/*private class OperationTask extends Thread {
		public void run() {
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			doInBackground();
		}

		boolean isCancellled = false;

		public void cancel(boolean b) {
			isCancellled = b;

		}

		private CommunicationOperation mCommunicationOperation;
		private OperationHandler mOperationHandler;
		private final Context context;

		public OperationTask(CommunicationOperation communicationOperation,
				OperationHandler operationHandler, final Context context) {
			mCommunicationOperation = communicationOperation;
			mOperationHandler = operationHandler;
			this.context = context;
		}

		*/

    /**
     * Notifies the operation's handler that an error has occurred.
     *//*
		private void sendErrorMessageToHanlder(int operationId,
				ErrorType errorType, String errorMessage) {
			Message message = Message.obtain();
			message.what = MESSAGE_OPERATION_FAIL;
			message.arg1 = operationId;

			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_DATA_KEY_ERROR_TYPE, errorType);
			data.putString(MESSAGE_DATA_KEY_ERROR_DESCRIPTION, errorMessage);

			message.setData(data);
			mOperationHandler.sendMessage(message);
		}

		Message sucessMessage;

		protected Boolean doInBackground() {
			try {
				sessionValidation(mCommunicationOperation, context);
				// notifying for starting the operation.
				Message startMessage = Message.obtain();
				startMessage.arg1 = mCommunicationOperation.getOperationId();
				startMessage.what = MESSAGE_OPERATION_START;
				mOperationHandler.sendMessage(startMessage);
				String url = mCommunicationOperation.getServiceUrl(context);
				RequestMethod requestMethod = mCommunicationOperation
						.getRequestMethod();
				String requestBody = mCommunicationOperation.getRequestBody();
				String timestamp_cache = mCommunicationOperation
						.getTimeStampCache();
				Response response = null;
				try {
					response = performRequest(url, requestMethod, requestBody,
							context, timestamp_cache,mCommunicationOperation);
				} catch (SocketException exception1) {
					Logger.e(TAG,
							"The connection was reseted... try for the second time.");
					try {
						response = performRequest(url, requestMethod,
								requestBody, context, timestamp_cache,mCommunicationOperation);
					} catch (SocketException exception2) {
						Logger.e(TAG,
								"The connection was reseted... try fo the third time.");
						response = performRequest(url, requestMethod,
								requestBody, context, timestamp_cache,mCommunicationOperation);
					}
				}

				if(Logger.isDebuggable){
					String temp[] = url.split("/");
					LogUtil.splitAndLog(TAG, temp[temp.length - 1] + " response "
							+ response);
				}
				
				Map<String, Object> responseData = mCommunicationOperation
						.parseResponse(response);
				response = null;
				// notifying for finishing the operation.
				sucessMessage = Message.obtain();
				sucessMessage.arg1 = mCommunicationOperation.getOperationId();
				sucessMessage.what = MESSAGE_OPERATION_SECCESS;
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_DATA_KEY_RESPONSE,
						(Serializable) responseData);
				sucessMessage.setData(data);
				onPostExecute(true);
			} catch (OperationCancelledException exception) {
				// operation has been cancelled.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.OPERATION_CANCELLED,
						"Operation has been cancelled.");

			} catch (InterruptedException exception) {
				// operation has been cancelled.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.OPERATION_CANCELLED,
						"Operation has been cancelled.");

			} catch (MalformedURLException exception) {
				// Bad Url.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
						"Invalid service URL.");

			} catch (SocketTimeoutException exception) {
				// timeout.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.NO_CONNECTIVITY, "No Internet Connection");

			} catch (IOException exception) {
				// File cannot be accessed or is corrupted..
				exception.printStackTrace();
//				if (mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
//						|| mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
//						|| mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
//					Map<String, Object> responseData = null;
//					try {
//						response.responseCode=RESPONSE_CONTENT_NOT_MODIFIED_304;
//						responseData = mCommunicationOperation
//								.parseResponse(response);
//					} catch (InvalidRequestParametersException e) {
//						e.printStackTrace();
//					} catch (InvalidRequestTokenException e) {
//						e.printStackTrace();
//					} catch (InvalidResponseDataException e) {
//						e.printStackTrace();
//					} catch (OperationCancelledException e) {
//						e.printStackTrace();
//					} catch (RecreateLoginException e) {
//						e.printStackTrace();
//					}
//					response = null;
//					// notifying for finishing the operation.
//					sucessMessage = Message.obtain();
//					sucessMessage.arg1 = mCommunicationOperation.getOperationId();
//					sucessMessage.what = MESSAGE_OPERATION_SECCESS;
//					Bundle data = new Bundle();
//					data.putSerializable(MESSAGE_DATA_KEY_RESPONSE,
//							(Serializable) responseData);
//					sucessMessage.setData(data);
//					onPostExecute(true);
//				}else{
					Logger.e(TAG, "No Internet Connection");
					sendErrorMessageToHanlder(
							mCommunicationOperation.getOperationId(),
							ErrorType.NO_CONNECTIVITY, "No Internet Connection");
					
//				}

				// Logger.e(TAG, "File cannot be accessed or is corrupted.");
				// sendErrorMessageToHanlder(mCommunicationOperation.getOperationId(),
				// ErrorType.NO_CONNECTIVITY,
				// " File cannot be accessed or is corrupted.");

			} catch (InvalidResponseDataException exception) {
				// Bad data from servers.
				Logger.e(TAG, "Bad response data from servers.");
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
						exception.getMessage());

			} catch (InvalidRequestParametersException exception) {
				// Bad request parameters.
				Logger.e(
						TAG,
						"Bad request parameters. "
								+ Integer.toString(exception.getCode()) + " "
								+ exception.getMessage());
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INVALID_REQUEST_PARAMETERS,
						exception.getMessage());

			} catch (InvalidRequestTokenException exception) {
				// Invalid request token.
				Logger.e(TAG, "Invalid request token.");
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.EXPIRED_REQUEST_TOKEN,
						"Invalid request token.");
			} catch (RecreateLoginException e) {
				Logger.printStackTrace(e);
				// ApplicationConfigurations appConfig = new
				// ApplicationConfigurations(context);
				// appConfig.setSessionID(null);
				// appConfig.setPasskey(null);
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.EXPIRED_REQUEST_TOKEN,
						"Invalid request token.");
			} catch (ContentNotAvailableException e) {
				Logger.e(TAG, "Bad response data from servers. : ContentNotAvailableException");
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.CONTENT_NOT_AVAILABLE,
						e.getMessage());
			}
			onPostExecute(false);
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				if (!isCancellled)
					mOperationHandler.sendMessage(sucessMessage);
			}
			if (running_Processes != null && running_Processes.contains(this))
				running_Processes.remove(this);

			OperationTask task = processesQueue.poll();
			if (task != null && running_Processes.size() < NUMBER_OF_CORES)
				task.start();

			// if(running_Processes)

		}

	}*/

    static class MyQueue implements Queue<OperationTask> {
        public MyQueue() {
            list = new Vector<CommunicationManager.OperationTask>();
        }

        Vector<OperationTask> list;

        @Override
        public boolean addAll(Collection<? extends OperationTask> collection) {
            return false;
        }

        @Override
        public void clear() {
            list.clear();
        }

        @Override
        public boolean contains(Object object) {
            return list.contains(object);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return list.containsAll(collection);
        }

        @Override
        public boolean isEmpty() {
            return list == null || list.size() == 0;
        }

        @Override
        public Iterator<OperationTask> iterator() {
            return list.iterator();
        }

        @Override
        public boolean remove(Object object) {
            return list.remove(object);
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return list.removeAll(collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return list.retainAll(collection);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return list.toArray(array);
        }

        @Override
        public boolean add(OperationTask e) {
            return list.add(e);
        }

        @Override
        public boolean offer(OperationTask e) {
            return false;
        }

        @Override
        public OperationTask remove() {

            return null;
        }

        @Override
        public OperationTask poll() {
            try {
                if (!isEmpty()) {
                    return list.remove(0);
                }
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        public OperationTask element() {
            try {
                if (!isEmpty()) {
                    return list.get(0);
                }
            } catch (Exception e) {
            }

            return null;

        }

        @Override
        public OperationTask peek() {
            try {
                if (!isEmpty()) {
                    return list.get(0);
                }
            } catch (Exception e) {
            }
            return null;
        }
    }

    // ======================================================
    // SERVERS OPERATIONS.
    // ======================================================

    /**
     * Executes webservice calls and retrieves responses.
     *
     * @param stringUrl
     * to the webservice.
     * @param parameters
     * for HTTP POST request.
     * @return response as string, formatted in "UTF-8".
     * @throws MalformedURLException
     * for invalid given URL.
     * @throws ProtocolException
     * for problems with parameters when performing HTTP POST calls.
     * @throws IOException
     * for connectivity and general I/O problems.
     * @throws OperationCancelledException
     */
    private boolean isExeption = false;
    private boolean returnResponse = false;

    public static class Response {
        public Response() {
            // TODO Auto-generated constructor stub
            response = "";
            responseCode = RESPONSE_SUCCESS_200;
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return response;
        }

        public String response;
        public int responseCode;
    }

    Response response;

    private Response performRequest(String stringUrl,
                                    RequestMethod requestMethod, String requestBody, Context context,
                                    String last_timestamp_cache, CommunicationOperation mCommunicationOperation) throws MalformedURLException,
            ProtocolException, IOException, SocketTimeoutException,
            InterruptedException, OperationCancelledException {

        Logger.i(TAG, "last_timestamp_cache -performRequest-- "
                + last_timestamp_cache);

        int retryCount = ApplicationConfigurations.getInstance(context)
                .getRetry();
        if (retryCount <= 0) {
            retryCount = 1;
        }
        for (int i = 0; i < retryCount; i++) {
            Logger.s(i + " performRequest:");
            if (isExeption) {
                isExeption = false;
                request(stringUrl, requestMethod, requestBody, context,
                        last_timestamp_cache, mCommunicationOperation);
            } else {
                isExeption = false;
                request(stringUrl, requestMethod, requestBody, context,
                        last_timestamp_cache, mCommunicationOperation);
            }
            if (returnResponse) {
                break;
            }
            Thread.sleep(1000);
        }
        Logger.s("response performRequest:" + response);
        return response;
    }

    private void enableHttpCaching(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                File httpCacheDir = new File(context.getApplicationContext()
                        .getCacheDir(), "http");
                long httpCacheSize = 20 * 1024 * 1024; // 10 MiB
                HttpResponseCache.install(httpCacheDir, httpCacheSize);
            } catch (IOException e) {
                Logger.i("", "OVER ICS: HTTP response cache failed:" + e);
            }
        }
        // else
        // {
        // File httpCacheDir = new
        // File(context.getApplicationContext().getCacheDir()
        // , "http");
        // try {
        // com.integralblue.httpresponsecache.HttpResponseCache.install
        // (httpCacheDir, 10 * 1024 * 1024);
        // } catch (IOException e) {
        // Log.i(""
        // , "UNDER ICS : HTTP response cache  failed:" + e);
        // }
        // }
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Request.Builder getRequestBuilder(Context context, URL url) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        String versionName = "";
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        requestBuilder.addHeader("DEVICE-OS", Utils.DEVICE_OS);
        requestBuilder.addHeader("APP-VERSION", versionName);
        requestBuilder.addHeader("DEVICE-MODEL",Utils.getDeviceName());
        requestBuilder.addHeader("BUILD-DEVICE", ""+Build.DEVICE);
        requestBuilder.addHeader("BUILD-PRODUCT", ""+Build.PRODUCT);
        requestBuilder.addHeader("BUILD-MODEL", Build.MODEL);
        requestBuilder.addHeader("BUILD-MANUFACTURER", Build.MANUFACTURER);
        requestBuilder.addHeader("BUILD-ID", Build.ID);

        ApplicationConfigurations config = ApplicationConfigurations.getInstance(context);
        if (!TextUtils.isEmpty(config.getDefaultUserAgent()))
            requestBuilder.addHeader("User-Agent", config.getDefaultUserAgent());
        return requestBuilder;
    }

//	/* This interceptor adds a custom User-Agent. */
//	public class UserAgentInterceptor implements Interceptor {
//		private final String userAgent;
//
//		public UserAgentInterceptor(String userAgent) {
//			this.userAgent = userAgent;
//		}
//
//		@Override
//		public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
//			Request originalRequest = chain.request();
//			Request requestWithUserAgent = originalRequest.newBuilder()
//					.removeHeader("User-Agent")
//					.addHeader("User-Agent", userAgent)
//					.build();
//			return chain.proceed(requestWithUserAgent);
//		}
//	}

    private void request(String stringUrl, RequestMethod requestMethod,
                         String requestBody, Context context, String last_timestamp_cache, CommunicationOperation mCommunicationOperation)
            throws MalformedURLException, ProtocolException, IOException,
            SocketTimeoutException, InterruptedException,
            OperationCancelledException {
        if (!Logger.enableOkHTTP) {
            requestOld(stringUrl, requestMethod, requestBody, context, last_timestamp_cache, mCommunicationOperation);
            return;
        }
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
//		HttpURLConnection connection = null;
//		OutputStreamWriter outputStream = null;
//		BufferedReader inputBufferedReader = null;
//		StringBuilder responseBuilder = new StringBuilder();

        OkHttpClient client = getUnsafeOkHttpClient();//new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
//				.url("http://www.publicobject.com/helloworld.txt")
//				.header("User-Agent", "OkHttp Example")
//				.build();

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        try {
            // builds the request.
            URL url = new URL(stringUrl);
            enableHttpCaching(context);
//			connection = createConnectionForURL(url);
            client.setCache(new Cache(context.getCacheDir(), 20 * 1024 * 1024L));
            requestBuilder.url(url);

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (!stringUrl.contains("//cdnapi.hungama.com/")) {

                ApplicationConfigurations config = ApplicationConfigurations
                        .getInstance(context);

                if (last_timestamp_cache == null)
                    last_timestamp_cache = "";
                String key = context.getString(R.string.key_billing)
                        .replaceAll(" ", "%");
                String versionName = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).versionName;

                if (stringUrl
                        .contains("http://apistaging.hungama.com/webservice/hungama/")
                        || stringUrl
                        .contains("http://api.hungama.com/webservice/hungama/")
                        || stringUrl
                        .contains("http://202.87.41.147/hungamacm_signup/spa/user_profile.php")) {
                    key = SECRET_KEY;
                    String userId = config.getPartnerUserId();
                    String md5;
                    if((mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE ||
                            mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES ||
                            mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) &&
                            !userId.equals(((HungamaWrapperOperation) mCommunicationOperation).getUserId())) {
                        userId = ((HungamaWrapperOperation) mCommunicationOperation).getUserId();
                        Logger.i(
                                TAG,
                                "md5 before: "
                                        + (key + userId));
                        md5 = Utils.toMD5(key + userId);
                        last_timestamp_cache = "";
                    } else {
                        Logger.i(
                                TAG,
                                "md5 before: "
                                        + (key + userId + last_timestamp_cache));
                        md5 = Utils.toMD5(key + userId
                                + last_timestamp_cache);
                    }

                    Logger.i(TAG, "md5: " + md5);

//					connection.addRequestProperty("API-KEY", md5);
                    requestBuilder.addHeader("API-KEY", md5);
                } else if (stringUrl.contains(context.getString(R.string.hungama_pay_url)) ||
                        stringUrl.contains(context.getString(R.string.hungama_pay_url_telco_api))) {
                    key = SECRET_KEY_PAY;
                    Logger.i(
                            TAG,
                            "md5 before: "
                                    + (key + config.getPartnerUserId()));
                    String md5 = Utils.toMD5(key + config.getPartnerUserId());

                    Logger.i(TAG, "md5: " + md5);

//					connection.addRequestProperty("API-KEY", md5);
                    requestBuilder.addHeader("API-KEY", md5);
                } else {
//					connection.addRequestProperty("API-KEY", key);
                    requestBuilder.addHeader("API-KEY", key);
                }
                if (config.getConsumerID() != 0) {
//					connection.addRequestProperty("CONSUMER-ID", "" + config.getConsumerID());
                    requestBuilder.addHeader("CONSUMER-ID", "" + config.getConsumerID());
                }

                requestBuilder.addHeader("DEVICE-OS", Utils.DEVICE_OS);
                requestBuilder.addHeader("APP-VERSION", versionName);
                requestBuilder.addHeader("DEVICE-MODEL", Utils.getDeviceName());
                requestBuilder.addHeader("BUILD-DEVICE", ""+Build.DEVICE);
                requestBuilder.addHeader("BUILD-PRODUCT", ""+Build.PRODUCT);
                requestBuilder.addHeader("BUILD-MODEL", Build.MODEL);
                requestBuilder.addHeader("BUILD-MANUFACTURER", Build.MANUFACTURER);
                requestBuilder.addHeader("BUILD-ID", Build.ID);
//				connection.addRequestProperty("Accept-Encoding", "gzip");

                // last_timestamp_cache="1426845656";

                Logger.i(TAG, "last_timestamp_cache**: " + last_timestamp_cache);

                if (!TextUtils.isEmpty(last_timestamp_cache))
                    requestBuilder.addHeader("LAST-CACHE",
                            last_timestamp_cache);

                // Map<String, List<String>> prop = connection
                // .getRequestProperties();
                // Set<String> keys = prop.keySet();
                // for (String pro : keys) {
                // List<String> values = prop.get(pro);
                // for (String value : values) {
                // System.out.println("property : " + pro + "   Value :: "
                // + value);
                // }
                // }

                if (!TextUtils.isEmpty(config.getDefaultUserAgent()))
                    requestBuilder.addHeader("User-Agent", config.getDefaultUserAgent());
            }
//			connection.setRequestMethod(requestMethod.toString());
            client.setConnectTimeout(getConnectionTimeout(context), TimeUnit.MILLISECONDS);
            client.setReadTimeout(getConnectionTimeout(context), TimeUnit.MILLISECONDS);
//			connection.setConnectTimeout(getConnectionTimeout(context));
//			connection.setReadTimeout(getConnectionTimeout(context));
//
//			// connection.setRequestProperty(CONNECTION_PROPERTY,
//			// CONNECTION_VALUE);
//			// connection.setRequestProperty("User-Agent","Hungama 3.0 (iPhone; iPhone OS 6.1.2; en_US)");
//			// requests the connection to be closed after and not header clear.
//
//			connection.setDefaultUseCaches(false);
//			connection.setUseCaches(false);
//
//			// adds additional properties to the request if the
//			// method is POST.
//			if (requestMethod == RequestMethod.POST) {
//				requestBuilder.addHeader(
//						REQUEST_PROPERTY_CONTENT_TYPE_KEY,
//						REQUEST_PROPERTY_CONTENT_TYPE_VALUE);
//				connection.setDoOutput(true);
//			}

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // posts the request and gets the response.
            if (requestMethod == RequestMethod.POST) {
                String temp[] = stringUrl.split("/");
                Logger.i(TAG, temp[temp.length - 1] + " request " + requestBody
                        + " to URL: " + stringUrl);
                if (temp[temp.length - 1].contains("Campaign")) {
                    Logger.writetofileCampaign(new Date() + " ::: " + " request "
                            + requestBody + " to URL: " + stringUrl, true);
                }
                Logger.writetofile(TAG, new Date() + " ::: " + " request "
                        + requestBody + " to URL: " + stringUrl);

//				BufferedOutputStream bufferedOutputStream = null;
//				/*
//				 * By default pre gingerbread devices, not explicitly define a
//				 * safe to use buffer size. seems like a bug.
//				 */
//				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//					bufferedOutputStream = new BufferedOutputStream(
//							connection.getOutputStream(), DEFAULT_BUFFER_SIZE);
//				} else {
//					bufferedOutputStream = new BufferedOutputStream(
//							connection.getOutputStream());
//				}
//				outputStream = new OutputStreamWriter(bufferedOutputStream);
//				outputStream.write(requestBody);
//				outputStream.flush();
                RequestBody body = RequestBody.create(MediaType.parse(REQUEST_PROPERTY_CONTENT_TYPE_VALUE), requestBody);
                requestBuilder.post(body);
            } else {
                Logger.i(TAG, "Getting from URL: " + stringUrl);
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

//			Request request = requestBuilder.build();
//			Headers headers = request.headers();
//			Set<String> heds = headers.names();
//			for(String head : heds)
//				Logger.s("UserAgent :::::::::::::::: " + head);
//			Logger.s("UserAgent :::::::::::::::: ------- ");//User-Agentokhttp/2.5.0
            com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
            response = new Response();
            response.responseCode = responseOk.code();
            Logger.i(TAG, "Response Code: " + response.responseCode);
            if (response.responseCode == RESPONSE_CONTENT_NOT_MODIFIED_304
                    || response.responseCode == RESPONSE_SERVER_ERROR_500) {
                returnResponse = true;
                return;
            } else if (response.responseCode == RESPONSE_NO_CONTENT_204) {
                returnResponse = true;
                return;
            } else if (response.responseCode == RESPONSE_BAD_REQUEST_400) {
                returnResponse = true;
                return;
            } else if (response.responseCode == RESPONSE_FORBIDDEN_403) {
                throw new IOException();
            }
            response.response = responseOk.body().string();


//			response = new Response();
//			response.responseCode = connection.getResponseCode();
//			Logger.i(TAG, "Response Code: " + connection.getResponseCode());
//			if (connection.getResponseCode() == RESPONSE_CONTENT_NOT_MODIFIED_304
//					|| connection.getResponseCode() == RESPONSE_SERVER_ERROR_500) {
//				returnResponse = true;
//				return;
//			} else if (connection.getResponseCode() == RESPONSE_NO_CONTENT_204) {
//				returnResponse = true;
//				return;
//			} else if (connection.getResponseCode() == RESPONSE_BAD_REQUEST_400) {
//				returnResponse = true;
//				return;
//			} else if (connection.getResponseCode() == RESPONSE_FORBIDDEN_403) {
//				throw new IOException();
//			}
//
//			InputStreamReader inputStreamReader = new InputStreamReader(
//					connection.getInputStream());
//			Logger.s("Encoding :::::::::::::: " + inputStreamReader.getEncoding()
//					+ " :: length : " + connection.getInputStream().available());
//			/*
//			 * By default pre gingerbread devices, not explicitly define a safe
//			 * to use buffer size. seems like a bug.
//			 */
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
//				inputBufferedReader = new BufferedReader(inputStreamReader,
//						DEFAULT_BUFFER_SIZE);
//			} else {
//				inputBufferedReader = new BufferedReader(inputStreamReader);
//			}
//
//			if (Thread.currentThread().isInterrupted()) {
//				throw new InterruptedException();
//			}
//
//			char[] BUFF = new char[500];
//			int len = 0;
//			while ((len = inputBufferedReader.read(BUFF)) > 0
//					&& !Thread.currentThread().isInterrupted()) {
//				responseBuilder.append(BUFF, 0, len);
//			}
//
//			response.response = responseBuilder.toString();

            String temp[] = stringUrl.split("/");
            if (temp[temp.length - 1].contains("Campaign")) {
                Logger.writetofileCampaign(new Date() + " ::: " + " response "
                        + response, true);
            }
            Logger.writetofile(TAG, new Date() + " ::: " + " response "
                    + response);

            if (Thread.currentThread().isInterrupted()) {
                throw new OperationCancelledException();
            }
        } catch (SocketTimeoutException e) {
            if (mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LET_MENU
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
                response = new Response();
                response.responseCode = RESPONSE_CONTENT_NOT_MODIFIED_304;
                response.response = "";
                isExeption = true;
                returnResponse = true;
                Logger.printStackTrace(e);
//                ProxyService.getInstance().deletePleaseWaitCommand();
                return;
            } else {
                isExeption = true;
                Logger.printStackTrace(e);
                throw e;
            }
        } catch (IOException e) {
            if (mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LET_MENU
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
                response = new Response();
                response.responseCode = RESPONSE_CONTENT_NOT_MODIFIED_304;
                if(mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS &&
                        e.getClass()==ProtocolException.class && e.getMessage().startsWith("HTTP 204 had non-zero Content-Length:")) {
                    response.responseCode = RESPONSE_NO_CONTENT_204;
                }
                response.response = "";
                returnResponse = true;
                isExeption = true;
                Logger.printStackTrace(e);
                return;
            } else {
                isExeption = true;
                Logger.printStackTrace(e);
                throw e;
            }
//			throw e;
        } catch (Exception e) {
            Logger.printStackTrace(e);
        } catch (Error e) {
            Logger.printStackTrace(e);
        } finally {
//			try {
//				if (inputBufferedReader != null) {
//					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
//						inputBufferedReader.close();
//					}
//					inputBufferedReader = null;
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			try {
//				if (outputStream != null) {
//					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
//						outputStream.close();
//					}
//					outputStream = null;
//				}
//			} catch (SocketException exception) { /* does nothing. */
//			}

            try {
                if (client != null) {
                    Logger.v(TAG, "releasing connection.");
//					client.disconnect();
                    client = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isExeption) {
            isExeption = false;
            returnResponse = true;
        }
    }

    private void requestOld(String stringUrl, RequestMethod requestMethod,
                            String requestBody, Context context, String last_timestamp_cache, CommunicationOperation mCommunicationOperation)
            throws MalformedURLException, ProtocolException, IOException,
            SocketTimeoutException, InterruptedException,
            OperationCancelledException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        HttpURLConnection connection = null;
        OutputStreamWriter outputStream = null;
        BufferedReader inputBufferedReader = null;
        StringBuilder responseBuilder = new StringBuilder();

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        try {
            // builds the request.
            URL url = new URL(stringUrl);
            enableHttpCaching(context);
            connection = createConnectionForURL(url);

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (!stringUrl.contains("//cdnapi.hungama.com/")) {

                ApplicationConfigurations config = ApplicationConfigurations
                        .getInstance(context);

                if (last_timestamp_cache == null)
                    last_timestamp_cache = "";
                String key = context.getString(R.string.key_billing)
                        .replaceAll(" ", "%");
                String versionName = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).versionName;

                if (stringUrl
                        .contains("http://apistaging.hungama.com/webservice/hungama/")
                        || stringUrl
                        .contains("http://api.hungama.com/webservice/hungama/")
                        || stringUrl
                        .contains("http://202.87.41.147/hungamacm_signup/spa/user_profile.php")) {
                    key = SECRET_KEY;
                    Logger.i(
                            TAG,
                            "md5 before: "
                                    + (key + config.getPartnerUserId() + last_timestamp_cache));
                    String md5 = Utils.toMD5(key + config.getPartnerUserId()
                            + last_timestamp_cache);

                    Logger.i(TAG, "md5: " + md5);

                    connection.addRequestProperty("API-KEY", md5);

                } else if (stringUrl.contains(context.getString(R.string.hungama_pay_url)) ||
                        stringUrl.contains(context.getString(R.string.hungama_pay_url_telco_api))) {
                    key = SECRET_KEY_PAY;
                    Logger.i(
                            TAG,
                            "md5 before: "
                                    + (key + config.getPartnerUserId()));
                    String md5 = Utils.toMD5(key + config.getPartnerUserId());

                    Logger.i(TAG, "md5: " + md5);

                    connection.addRequestProperty("API-KEY", md5);

                } else {
                    connection.addRequestProperty("API-KEY", key);
                }
                if (config.getConsumerID() != 0)
                    connection.addRequestProperty("CONSUMER-ID", "" + config.getConsumerID());

                connection.addRequestProperty("DEVICE-OS", Utils.DEVICE_OS);
                connection.addRequestProperty("APP-VERSION", versionName);
                connection.addRequestProperty("DEVICE-MODEL",
                        Utils.getDeviceName());
//				connection.addRequestProperty("Accept-Encoding", "gzip");

                // last_timestamp_cache="1426845656";

                Logger.i(TAG, "last_timestamp_cache**: " + last_timestamp_cache);

                if (!TextUtils.isEmpty(last_timestamp_cache))
                    connection.addRequestProperty("LAST-CACHE",
                            last_timestamp_cache);

                // Map<String, List<String>> prop = connection
                // .getRequestProperties();
                // Set<String> keys = prop.keySet();
                // for (String pro : keys) {
                // List<String> values = prop.get(pro);
                // for (String value : values) {
                // System.out.println("property : " + pro + "   Value :: "
                // + value);
                // }
                // }
            }
            connection.setRequestMethod(requestMethod.toString());
            connection.setConnectTimeout(getConnectionTimeout(context));
            connection.setReadTimeout(getConnectionTimeout(context));

            // connection.setRequestProperty(CONNECTION_PROPERTY,
            // CONNECTION_VALUE);
            // connection.setRequestProperty("User-Agent","Hungama 3.0 (iPhone; iPhone OS 6.1.2; en_US)");
            // requests the connection to be closed after and not header clear.

            connection.setDefaultUseCaches(false);
            connection.setUseCaches(false);

            // adds additional properties to the request if the
            // method is POST.
            if (requestMethod == RequestMethod.POST) {
                connection.setRequestProperty(
                        REQUEST_PROPERTY_CONTENT_TYPE_KEY,
                        REQUEST_PROPERTY_CONTENT_TYPE_VALUE);
                connection.setDoOutput(true);
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // posts the request and gets the response.
            if (requestMethod == RequestMethod.POST) {
                String temp[] = stringUrl.split("/");
                Logger.i(TAG, temp[temp.length - 1] + " request " + requestBody
                        + " to URL: " + stringUrl);
                if (temp[temp.length - 1].contains("Campaign")) {
                    Logger.writetofileCampaign(new Date() + " ::: " + " request "
                            + requestBody + " to URL: " + stringUrl, true);
                }
                Logger.writetofile(TAG, new Date() + " ::: " + " request "
                        + requestBody + " to URL: " + stringUrl);

                BufferedOutputStream bufferedOutputStream = null;
				/*
				 * By default pre gingerbread devices, not explicitly define a
				 * safe to use buffer size. seems like a bug.
				 */
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                    bufferedOutputStream = new BufferedOutputStream(
                            connection.getOutputStream(), DEFAULT_BUFFER_SIZE);
                } else {
                    bufferedOutputStream = new BufferedOutputStream(
                            connection.getOutputStream());
                }
                outputStream = new OutputStreamWriter(bufferedOutputStream);
                outputStream.write(requestBody);
                outputStream.flush();
            } else {
                Logger.i(TAG, "Getting from URL: " + stringUrl);
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            response = new Response();
            response.responseCode = connection.getResponseCode();
            Logger.i(TAG, "Response Code: " + connection.getResponseCode());
            if (connection.getResponseCode() == RESPONSE_CONTENT_NOT_MODIFIED_304
                    || connection.getResponseCode() == RESPONSE_SERVER_ERROR_500) {
                returnResponse = true;
                return;
            } else if (connection.getResponseCode() == RESPONSE_NO_CONTENT_204) {
                returnResponse = true;
                return;
            } else if (connection.getResponseCode() == RESPONSE_BAD_REQUEST_400) {
                returnResponse = true;
                return;
            } else if (connection.getResponseCode() == RESPONSE_FORBIDDEN_403) {
                throw new IOException();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(
                    connection.getInputStream());
            Logger.s("Encoding :::::::::::::: " + inputStreamReader.getEncoding()
                    + " :: length : " + connection.getInputStream().available());
			/*
			 * By default pre gingerbread devices, not explicitly define a safe
			 * to use buffer size. seems like a bug.
			 */
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                inputBufferedReader = new BufferedReader(inputStreamReader,
                        DEFAULT_BUFFER_SIZE);
            } else {
                inputBufferedReader = new BufferedReader(inputStreamReader);
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            char[] BUFF = new char[500];
            int len = 0;
            while ((len = inputBufferedReader.read(BUFF)) > 0
                    && !Thread.currentThread().isInterrupted()) {
                responseBuilder.append(BUFF, 0, len);
            }

            response.response = responseBuilder.toString();

            String temp[] = stringUrl.split("/");
            if (temp[temp.length - 1].contains("Campaign")) {
                Logger.writetofileCampaign(new Date() + " ::: " + " response "
                        + response, true);
            }
            Logger.writetofile(TAG, new Date() + " ::: " + " response "
                    + response);

            if (Thread.currentThread().isInterrupted()) {
                throw new OperationCancelledException();
            }
        } catch (SocketTimeoutException e) {
            if (mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LET_MENU
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
                response = new Response();
                response.responseCode = RESPONSE_CONTENT_NOT_MODIFIED_304;
                response.response = "";
                isExeption = true;
                returnResponse = true;
                Logger.printStackTrace(e);
                return;
            } else {
                isExeption = true;
                Logger.printStackTrace(e);
                throw e;
            }
        } catch (IOException e) {
            if (mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.LET_MENU
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS
                    || mCommunicationOperation.getOperationId() == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
                response = new Response();
                response.responseCode = RESPONSE_CONTENT_NOT_MODIFIED_304;
                response.response = "";
                returnResponse = true;
                isExeption = true;
                Logger.printStackTrace(e);
                return;
            } else {
                isExeption = true;
                Logger.printStackTrace(e);
                throw e;
            }
//			throw e;
        } catch (Exception e) {
            Logger.printStackTrace(e);
        } catch (Error e) {
            Logger.printStackTrace(e);
        } finally {
            try {
                if (inputBufferedReader != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                        inputBufferedReader.close();
                    }
                    inputBufferedReader = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (outputStream != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                        outputStream.close();
                    }
                    outputStream = null;
                }
            } catch (SocketException exception) { /* does nothing. */
            }

            try {
                if (connection != null) {
                    Logger.v(TAG, "releasing connection.");
                    connection.disconnect();
                    connection = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!isExeption) {
            isExeption = false;
            returnResponse = true;
        }
    }

    private static HttpURLConnection createConnectionForURL(URL url)
            throws IOException {
        // instantiating the URL Connection based on the protocol.
        HttpURLConnection connection = null;
        if (url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTP)) {
            connection = (HttpURLConnection) url.openConnection();

        } else if (url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTPS)) {
            // sets host verifier.
            // HttpsURLConnection
            // .setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
            try {// xtpl
//				HttpsURLConnection
//						.setDefaultHostnameVerifier(new HostnameVerifier() {
//							@Override
//							public boolean verify(String hostname,
//									SSLSession session) {
//								return true;
//							}
//						});
//				connection = (HttpsURLConnection) url.openConnection();
//				SSLContext context = SSLContext.getInstance("TLS");
//				context.init(null,
//						new X509TrustManager[] { new X509TrustManager() {
//							public void checkClientTrusted(
//									X509Certificate[] chain, String authType)
//									throws CertificateException {
//							}
//
//							public void checkServerTrusted(
//									X509Certificate[] chain, String authType)
//									throws CertificateException {
//							}
//
//							public X509Certificate[] getAcceptedIssuers() {
//								return new X509Certificate[0];
//							}
//						} }, new SecureRandom());
//				HttpsURLConnection.setDefaultSSLSocketFactory(context
//						.getSocketFactory());

                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname,
                                          SSLSession session) {
                        return true;
                    }
                });
                connection = https;

            } catch (Exception e) { // should never happen
                e.printStackTrace();
            }
        } else {
            throw new ProtocolException(
                    "Only http and https protocols are supported.");
        }

        return connection;
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getConnectionTimeout(Context context) {
        int timeout = ApplicationConfigurations.getInstance(context)
                .getTimeout();
        if (timeout < 10)
            timeout = 10;
        return timeout * 1000;
    }

    private void sessionValidation(CommunicationOperation operation,
                                   Context context) throws InvalidResponseDataException {
        if (operation instanceof CMDecoratorOperation) {
            // System.out
            // .println("True ::::::::::::::::::::::::::::::::: instanceof CMDecoratorOperation ::: "
            // + operation);
            if (operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.SESSION_CREATE
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.TIME_READ
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE
                    && operation.getOperationId() != OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN) {
                // System.out
                // .println(" ::::::::::::::::::::::::::::::::: Validating session "
                // + operation.getOperationId());
//                Map<String, Object> result = null;
                ApplicationConfigurations appConfig = ApplicationConfigurations
                        .getInstance(context);
                String sessionId = appConfig.getSessionID();
                String passkey = appConfig.getPasskey();
                if (sessionId == null
                        || (sessionId != null && (sessionId.length() == 0
                        || sessionId.equalsIgnoreCase("null") || sessionId
                        .equalsIgnoreCase("none")))) {
                    // try {
                    if (passkey != null
                            && !(passkey.length() == 0
                            || passkey.equalsIgnoreCase("null") || passkey
                            .equalsIgnoreCase("none"))) {
                        // System.out
                        // .println(" ::::::::::::::::::::::::::::::::: Creating session ");
                        try {
                            // gets the new session for the user.
                            /*result = */
                            performOperation(new CMDecoratorOperation(
                                            ServerConfigurations.getInstance(context)
                                                    .getServerUrl(),
                                            new SessionCreateOperation(context)),
                                    context);
                        } catch (InvalidRequestException e) {
                            Logger.printStackTrace(e);
                        } catch (InvalidResponseDataException e) {
                            Logger.printStackTrace(e);
                        } catch (OperationCancelledException e) {
                            Logger.printStackTrace(e);
                        } catch (NoConnectivityException e) {
                            Logger.printStackTrace(e);
                        }
//                        result = null;
                        sessionId = appConfig.getSessionID();
                        if (sessionId == null
                                || (sessionId != null && (sessionId.length() == 0
                                || sessionId.equalsIgnoreCase("null") || sessionId
                                .equalsIgnoreCase("none")))) {
                            throw new InvalidResponseDataException();
                        }
                    } else {
                        throw new InvalidResponseDataException();
                    }
                }
            }
        } else {
            // System.out
            // .println("False ::::::::::::::::::::::::::::::::: instanceof CMDecoratorOperation ::: "
            // + operation);
        }
    }
}
