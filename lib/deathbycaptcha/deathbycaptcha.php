<?php
/**
 * @package DBCAPI
 * @author Sergey Kolchin <ksa242@gmail.com>
 */

/**
 * Base class for DBC related exceptions.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
abstract class DeathByCaptcha_Exception extends Exception
{}


/**
 * Exception to throw on environment or runtime related failures.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_RuntimeException extends DeathByCaptcha_Exception
{}


/**
 * Exception to throw on network or disk IO failures.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_IOException extends DeathByCaptcha_Exception
{}


/**
 * Generic exception to throw on API client errors.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_ClientException extends DeathByCaptcha_Exception
{}


/**
 * Exception to throw on rejected login attemts due to invalid DBC credentials, low balance, or when account being banned.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_AccessDeniedException extends DeathByCaptcha_ClientException
{}


/**
 * Exception to throw on invalid CAPTCHA image payload: on empty images, on images too big, on non-image payloads.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_InvalidCaptchaException extends DeathByCaptcha_ClientException
{}


/**
 * Generic exception to throw on API server errors.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_ServerException extends DeathByCaptcha_Exception
{}


/**
 * Exception to throw when service is overloaded.
 *
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_ServiceOverloadException extends DeathByCaptcha_ServerException
{}


/**
 * Base Death by Captcha API client
 *
 * @property-read array|null $user    User's details
 * @property-read float|null $balance User's balance (in US cents)
 *
 * @package DBCAPI
 * @subpackage PHP
 */
abstract class DeathByCaptcha_Client
{
    const API_VERSION = 'DBC/PHP v4.1.1';

    const DEFAULT_TIMEOUT = 60;
    const POLLS_INTERVAL = 5;


    /**
     * DBC account credentials
     *
     * @var array
     */
    protected $_userpwd = array();


    /**
     * Verbosity flag.
     * When it's set to true, the client will produce debug output on every API call.
     *
     * @var bool
     */
    public $is_verbose = false;


    /**
     * Parses URL query encoded responses
     *
     * @param string $s
     * @return array
     */
    static public function parse_plain_response($s)
    {
        parse_str($s, $a);
        return $a;
    }

    /**
     * Parses JSON encoded response
     *
     * @param string $s
     * @return array
     */
    static public function parse_json_response($s)
    {
        return json_decode(rtrim($s), true);
    }


    /**
     * Checks if CAPTCHA is valid (not empty)
     *
     * @param string $img Raw CAPTCHA image
     * @throws DeathByCaptcha_InvalidCaptchaException On invalid CAPTCHA images
     */
    protected function _is_valid_captcha($img)
    {
        if (0 == strlen($img)) {
            throw new DeathByCaptcha_InvalidCaptchaException(
                'CAPTCHA image file is empty'
            );
        } else {
            return true;
        }
    }

    protected function _load_captcha($captcha)
    {
        if (is_resource($captcha)) {
            $img = '';
            rewind($captcha);
            while ($s = fread($captcha, 8192)) {
                $img .= $s;
            }
            return $img;
        } else if (is_array($captcha)) {
            return implode('', array_map('chr', $captcha));
        } else if ('base64:' == substr($captcha, 0, 7)) {
            return base64_decode(substr($captcha, 7));
        } else {
            return file_get_contents($captcha);
        }
    }


    /**
     * Closes opened connection (if any), as gracefully as possible
     *
     * @return DeathByCaptcha_Client
     */
    abstract public function close();

    /**
     * Returns user details
     *
     * @return array|null
     */
    abstract public function get_user();

    /**
     * Returns user's balance (in US cents)
     *
     * @uses DeathByCaptcha_Client::get_user()
     * @return float|null
     */
    public function get_balance()
    {
        return ($user = $this->get_user()) ? $user['balance'] : null;
    }

    /**
     * Returns CAPTCHA details
     *
     * @param int $cid CAPTCHA ID
     * @return array|null
     */
    abstract public function get_captcha($cid);

    /**
     * Returns CAPTCHA text
     *
     * @uses DeathByCaptcha_Client::get_captcha()
     * @param int $cid CAPTCHA ID
     * @return string|null
     */
    public function get_text($cid)
    {
        return ($captcha = $this->get_captcha($cid)) ? $captcha['text'] : null;
    }

    /**
     * Reports an incorrectly solved CAPTCHA
     *
     * @param int $cid CAPTCHA ID
     * @return bool
     */
    abstract public function report($cid);

    /**
     * Uploads a CAPTCHA
     *
     * @param string|array|resource $captcha CAPTCHA image file name, vector of bytes, or file handle
     * @return array|null Uploaded CAPTCHA details on success
     * @throws DeathByCaptcha_InvalidCaptchaException On invalid CAPTCHA file
     */
    abstract public function upload($captcha);

    /**
     * Tries to solve CAPTCHA by uploading it and polling for its status/text
     * with arbitrary timeout. See {@link DeathByCaptcha_Client::upload()} for
     * $captcha param details.
     *
     * @uses DeathByCaptcha_Client::upload()
     * @uses DeathByCaptcha_Client::get_captcha()
     * @param int $timeout Optional solving timeout (in seconds)
     * @return array|null CAPTCHA details hash on success
     */
    public function decode($captcha, $timeout=self::DEFAULT_TIMEOUT)
    {
        $deadline = time() + (0 < $timeout ? $timeout : self::DEFAULT_TIMEOUT);
        if ($c = $this->upload($captcha)) {
            while ($deadline > time() && $c && !$c['text']) {
                sleep(self::POLLS_INTERVAL);
                $c = $this->get_captcha($c['captcha']);
            }
            if ($c && $c['text'] && $c['is_correct']) {
                return $c;
            }
        }
        return null;
    }

    /**
     * @param string $username DBC account username
     * @param string $password DBC account password
     * @throws DeathByCaptcha_RuntimeException On missing/empty DBC account credentials
     * @throws DeathByCaptcha_RuntimeException When required extensions/functions not found
     */
    public function __construct($username, $password)
    {
        foreach (array('username', 'password') as $k) {
            if (!$$k) {
                throw new DeathByCaptcha_RuntimeException(
                    "Account {$k} is missing or empty"
                );
            }
        }
        $this->_userpwd = array($username, $password);
    }

    /**
     * @ignore
     */
    public function __destruct()
    {
        $this->close();
    }

    /**
     * @ignore
     */
    public function __get($key)
    {
        switch ($key) {
        case 'user':
            return $this->get_user();
        case 'balance':
            return $this->get_balance();
        }
    }
}


/**
 * Death by Captcha HTTP API Client
 *
 * @see DeathByCaptcha_Client
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_HttpClient extends DeathByCaptcha_Client
{
    const BASE_URL = 'http://api.dbcapi.me/api';


    protected $_conn = null;
    protected $_response_type = '';
    protected $_response_parser = null;


    /**
     * Sets up CURL connection
     */
    protected function _connect()
    {
        if (!is_resource($this->_conn)) {
            if ($this->is_verbose) {
                fputs(STDERR, time() . " CONN\n");
            }

            if (!($this->_conn = curl_init())) {
                throw new DeathByCaptcha_RuntimeException(
                    'Failed initializing a CURL connection'
                );
            }

            curl_setopt_array($this->_conn, array(
                CURLOPT_TIMEOUT => self::DEFAULT_TIMEOUT,
                CURLOPT_CONNECTTIMEOUT => (int)(self::DEFAULT_TIMEOUT / 4),
                CURLOPT_HEADER => false,
                CURLOPT_RETURNTRANSFER => true,
                CURLOPT_FOLLOWLOCATION => true,
                CURLOPT_AUTOREFERER => false,
                CURLOPT_HTTPHEADER => array(
                    'Accept: ' . $this->_response_type,
                    'Expect: ',
                    'User-Agent: ' . self::API_VERSION
                )
            ));
        }

        return $this;
    }

    /**
     * Makes an API call
     *
     * @param string $cmd     API command
     * @param array  $payload API call payload, essentially HTTP POST fields
     * @return array|null API response hash table on success
     * @throws DeathByCaptcha_IOException On network related errors
     * @throws DeathByCaptcha_AccessDeniedException On failed login attempt
     * @throws DeathByCaptcha_InvalidCaptchaException On invalid CAPTCHAs rejected by the service
     * @throws DeathByCaptcha_ServerException On API server errors
     */
    protected function _call($cmd, $payload=null)
    {
        if (null !== $payload) {
            $payload = array_merge($payload, array(
                'username' => $this->_userpwd[0],
                'password' => $this->_userpwd[1],
            ));
        }

        $this->_connect();

        $opts = array(CURLOPT_URL => self::BASE_URL . '/' . trim($cmd, '/'),
                      CURLOPT_REFERER => '');
        if (null !== $payload) {
            $opts[CURLOPT_POST] = true;
            $opts[CURLOPT_POSTFIELDS] = array_key_exists('captchafile', $payload)
                ? $payload
                : http_build_query($payload);
        } else {
            $opts[CURLOPT_HTTPGET] = true;
        }
        curl_setopt_array($this->_conn, $opts);

        if ($this->is_verbose) {
            fputs(STDERR, time() . " SEND: {$cmd} " . serialize($payload) . "\n");
        }

        $response = curl_exec($this->_conn);
        if (0 < ($err = curl_errno($this->_conn))) {
            throw new DeathByCaptcha_IOException(
                "API connection failed: [{$err}] " . curl_error($this->_conn)
            );
        }

        if ($this->is_verbose) {
            fputs(STDERR, time() . " RECV: {$response}\n");
        }

        $status_code = curl_getinfo($this->_conn, CURLINFO_HTTP_CODE);
        if (403 == $status_code) {
            throw new DeathByCaptcha_AccessDeniedException(
                'Access denied, check your credentials and/or balance'
            );
        } else if (400 == $status_code || 413 == $status_code) {
            throw new DeathByCaptcha_InvalidCaptchaException(
                "CAPTCHA was rejected by the service, check if it's a valid image"
            );
        } else if (503 == $status_code) {
            throw new DeathByCaptcha_ServiceOverloadException(
                "CAPTCHA was rejected due to service overload, try again later"
            );
        } else if (!($response = call_user_func($this->_response_parser, $response))) {
            throw new DeathByCaptcha_ServerException(
                'Invalid API response'
            );
        } else {
            return $response;
        }
    }


    /**
     * @see DeathByCaptcha_Client::__construct()
     */
    public function __construct($username, $password)
    {
        if (!extension_loaded('curl')) {
            throw new DeathByCaptcha_RuntimeException(
                'CURL extension not found'
            );
        }
        if (function_exists('json_decode')) {
            $this->_response_type = 'application/json';
            $this->_response_parser = array($this, 'parse_json_response');
        } else {
            $this->_response_type = 'text/plain';
            $this->_response_parser = array($this, 'parse_plain_response');
        }
        parent::__construct($username, $password);
    }

    /**
     * @see DeathByCaptcha_Client::close()
     */
    public function close()
    {
        if (is_resource($this->_conn)) {
            if ($this->is_verbose) {
                fputs(STDERR, time() . " CLOSE\n");
            }
            curl_close($this->_conn);
            $this->_conn = null;
        }
        return $this;
    }

    /**
     * @see DeathByCaptcha_Client::get_user()
     */
    public function get_user()
    {
        $user = $this->_call('user', array());
        return (0 < ($id = (int)@$user['user']))
            ? array('user' => $id,
                    'balance' => (float)@$user['balance'],
                    'is_banned' => (bool)@$user['is_banned'])
            : null;
    }

    /**
     * @see DeathByCaptcha_Client::upload()
     * @throws DeathByCaptcha_RuntimeException When failed to save CAPTCHA image to a temporary file
     */
    public function upload($captcha)
    {
        $img = $this->_load_captcha($captcha);
        if ($this->_is_valid_captcha($img)) {
            $tmp_fn = tempnam(null, 'captcha');
            file_put_contents($tmp_fn, $img);
            try {
                $captcha = $this->_call('captcha', array(
                    'captchafile' => '@'. $tmp_fn,
                ));
            } catch (Exception $e) {
                @unlink($tmp_fn);
                throw $e;
            }
            @unlink($tmp_fn);
            if (0 < ($cid = (int)@$captcha['captcha'])) {
                return array(
                    'captcha' => $cid,
                    'text' => (!empty($captcha['text']) ? $captcha['text'] : null),
                    'is_correct' => (bool)@$captcha['is_correct'],
                );
            }
        }
        return null;
    }

    /**
     * @see DeathByCaptcha_Client::get_captcha()
     */
    public function get_captcha($cid)
    {
        $captcha = $this->_call('captcha/' . (int)$cid);
        return (0 < ($cid = (int)@$captcha['captcha']))
            ? array('captcha' => $cid,
                    'text' => (!empty($captcha['text']) ? $captcha['text'] : null),
                    'is_correct' => (bool)$captcha['is_correct'])
            : null;
    }

    /**
     * @see DeathByCaptcha_Client::report()
     */
    public function report($cid)
    {
        $captcha = $this->_call('captcha/' . (int)$cid . '/report', array());
        return !(bool)@$captcha['is_correct'];
    }
}


/**
 * Death by Captcha socket API Client
 *
 * @see DeathByCaptcha_Client
 * @package DBCAPI
 * @subpackage PHP
 */
class DeathByCaptcha_SocketClient extends DeathByCaptcha_Client
{
    const HOST = 'api.dbcapi.me';
    const FIRST_PORT = 8123;
    const LAST_PORT = 8130;

    const TERMINATOR = "\r\n";


    protected $_sock = null;


    /**
     * Opens a socket connection to the API server
     *
     * @throws DeathByCaptcha_IOException When API connection fails
     * @throws DeathByCaptcha_RuntimeException When socket operations fail
     */
    protected function _connect()
    {
        if (null === $this->_sock) {
            if ($this->is_verbose) {
                fputs(STDERR, time() . " CONN\n");
            }

            $errno = 0;
            $error = '';
            $port = rand(self::FIRST_PORT, self::LAST_PORT);
            $sock = null;

            if (!($sock = @fsockopen(self::HOST, $port, $errno, $error, self::DEFAULT_TIMEOUT))) {
                throw new DeathByCaptcha_IOException(
                    'Failed connecting to ' . self::HOST . ":{$port}: fsockopen(): [{$errno}] {$error}"
                );
            } else if (!@stream_set_timeout($sock, self::DEFAULT_TIMEOUT / 4)) {
                fclose($sock);
                throw new DeathByCaptcha_IOException(
                    'Failed setting socket timeout'
                );
            } else {
                $this->_sock = $sock;
            }
        }

        return $this;
    }

    /**
     * Socket send()/recv() wrapper
     *
     * @param string $buf Raw API request to send
     * @return string Raw API response on success
     * @throws DeathByCaptcha_IOException On network failures
     */
    protected function _sendrecv($buf)
    {
        if ($this->is_verbose) {
            fputs(STDERR, time() . ' SEND: ' . strlen($buf) . ' ' . rtrim($buf) . "\n");
        }

        $buf .= self::TERMINATOR;
        $response = '';
        while (true) {
            if ($buf) {
                if (!($n = fwrite($this->_sock, $buf))) {
                    throw new DeathByCaptcha_IOException(
                        'Connection lost while sending API request'
                    );
                } else {
                    $buf = substr($buf, $n);
                }
            }
            if (!$buf) {
                if (!($s = fread($this->_sock, 4096))) {
                    throw new DeathByCaptcha_IOException(
                        'Connection lost while receiving API response'
                    );
                } else {
                    $response .= $s;
                    if (self::TERMINATOR == substr($s, strlen($s) - 2)) {
                        $response = rtrim($response, self::TERMINATOR);
                        if ($this->is_verbose) {
                            fputs(STDERR, time() . ' RECV: ' . strlen($response) . " {$response}\n");
                        }
                        return $response;
                    }
                }
            }
        }

        throw new DeathByCaptcha_IOException('API request timed out');
    }

    /**
     * Makes an API call
     *
     * @param string $cmd     API command to call
     * @param array  $payload API request payload
     * @return array|null API response hash map on success
     * @throws DeathByCaptcha_IOException On network errors
     * @throws DeathByCaptcha_AccessDeniedException On failed login attempt
     * @throws DeathByCaptcha_InvalidCaptchaException On invalid CAPTCHAs rejected by the service
     * @throws DeathByCaptcha_ServerException On API server errors
     */
    protected function _call($cmd, $payload=null)
    {
        if (null === $payload) {
            $payload = array();
        }
        $payload = array_merge($payload, array(
            'cmd' => $cmd,
            'version' => self::API_VERSION,
        ));
        $payload = json_encode($payload);

        $response = null;
        for ($attempt = 2; 0 < $attempt && null === $response; $attempt--) {
            if (null === $this->_sock && 'login' != $cmd) {
                $this->_call('login', array(
                    'username' => $this->_userpwd[0],
                    'password' => $this->_userpwd[1],
                ));
            }
            $this->_connect();
            try {
                $response = $this->_sendrecv($payload);
            } catch (DeathByCaptcha_Exception $e) {
                $this->close();
            }
        }

        try {
            if (null === $response) {
                throw new DeathByCaptcha_IOException(
                    'API connection lost or timed out'
                );
            } else if (!($response = $this->parse_json_response($response))) {
                throw new DeathByCaptcha_ServerException(
                    'Invalid API response'
                );
            }

            if (!empty($response['error'])) {
                switch ($response['error']) {
                case 'not-logged-in':
                    throw new DeathByCaptcha_AccessDeniedException(
                        'Access denied, check your credentials'
                    );
                case 'banned':
                    throw new DeathByCaptcha_AccessDeniedException(
                        'Access denied, account suspended'
                    );
                case 'insufficient-funds':
                    throw new DeathByCaptcha_AccessDeniedException(
                        'Access denied, balance is too low'
                    );
                case 'invalid-captcha':
                    throw new DeathByCaptcha_InvalidCaptchaException(
                        "CAPTCHA was rejected by the service, check if it's a valid image"
                    );
                case 'service-overload':
                    throw new DeathByCaptcha_ServiceOverloadException(
                        'CAPTCHA was rejected due to service overload, try again later'
                    );
                default:
                    throw new DeathByCaptcha_ServerException(
                        'API server error occured: ' . $error
                    );
                }
            } else {
                return $response;
            }
        } catch (Exception $e) {
            $this->close();
            throw $e;
        }
    }


    /**
     * @see DeathByCaptcha_Client::__construct()
     */
    public function __construct($username, $password)
    {
        // PHP for Windows lacks EAGAIN errno constant
        if (!defined('SOCKET_EAGAIN')) {
            define('SOCKET_EAGAIN', 11);
        }

        foreach (array('json', ) as $k) {
            if (!extension_loaded($k)) {
                throw new DeathByCaptcha_RuntimeException(
                    "Required {$k} extension not found, check your PHP configuration"
                );
            }
        }
        foreach (array('json_encode', 'json_decode', 'base64_encode') as $k) {
            if (!function_exists($k)) {
                throw new DeathByCaptcha_RuntimeException(
                    "Required {$k}() function not found, check your PHP configuration"
                );
            }
        }

        parent::__construct($username, $password);
    }

    /**
     * @see DeathByCaptcha_Client::close()
     */
    public function close()
    {
        if (null !== $this->_sock) {
            if ($this->is_verbose) {
                fputs(STDERR, time() . " CLOSE\n");
            }

            fclose($this->_sock);
            $this->_sock = null;
        }

        return $this;
    }

    /**
     * @see DeathByCaptcha_Client::get_user()
     */
    public function get_user()
    {
        $user = $this->_call('user');
        return (0 < ($id = (int)@$user['user']))
            ? array('user' => $id,
                    'balance' => (float)@$user['balance'],
                    'is_banned' => (bool)@$user['is_banned'])
            : null;
    }

    /**
     * @see DeathByCaptcha_Client::get_user()
     */
    public function upload($captcha)
    {
        $img = $this->_load_captcha($captcha);
        if ($this->_is_valid_captcha($img)) {
            $captcha = $this->_call('upload', array(
                'captcha' => base64_encode($img),
            ));
            if (0 < ($cid = (int)@$captcha['captcha'])) {
                return array(
                    'captcha' => $cid,
                    'text' => (!empty($captcha['text']) ? $captcha['text'] : null),
                    'is_correct' => (bool)@$captcha['is_correct'],
                );
            }
        }
        return null;
    }

    /**
     * @see DeathByCaptcha_Client::get_captcha()
     */
    public function get_captcha($cid)
    {
        $captcha = $this->_call('captcha', array('captcha' => (int)$cid));
        return (0 < ($cid = (int)@$captcha['captcha']))
            ? array('captcha' => $cid,
                    'text' => (!empty($captcha['text']) ? $captcha['text'] : null),
                    'is_correct' => (bool)$captcha['is_correct'])
            : null;
    }

    /**
     * @see DeathByCaptcha_Client::report()
     */
    public function report($cid)
    {
        $captcha = $this->_call('report', array('captcha' => (int)$cid));
        return !@$captcha['is_correct'];
    }
}
