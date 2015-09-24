<?php
class Vplaza_Restapi_IndexController extends Mage_Core_Controller_Front_Action{

    const XML_PATH_EMAIL_RECIPIENT  = 'contacts/email/recipient_email';
    const XML_PATH_EMAIL_SENDER     = 'contacts/email/sender_email_identity';
    const XML_PATH_EMAIL_TEMPLATE   = 'contacts/email/email_template';
    const XML_PATH_ENABLED          = 'contacts/contacts/enabled';

    public function tokenval()
    {
        return md5('9887402381');
    }

    public function indexAction() {

        //Basic parameters that need to be provided for oAuth authentication
        //on Magento

        /*$params = array(
            'siteUrl' => Mage::getBaseUrl().'oauth',
            'requestTokenUrl' => Mage::getBaseUrl().'oauth/initiate',
            'accessTokenUrl' => Mage::getBaseUrl().'oauth/token',
            'authorizeUrl' => Mage::getBaseUrl().'admin/oauth_authorize', //This URL is used only if we authenticate as Admin user type
            'consumerKey' => 'e996e92b728faac307eb86b7b015087a', //7d847a959fe5d9ea2a39751af2d0f8b8 Consumer key registered in server administration
            'consumerSecret' => '20c61e1ff1d130e4c7b8076e9873f49c', //041b68bca247bee7d733fb7c17fe8028 Consumer secret registered in server administration
            'callbackUrl' => Mage::getBaseUrl().'restapi/index/callback', //Url of callback action below
        );


        $oAuthClient = Mage::getModel('restapi/api');
        $oAuthClient->reset();

        $oAuthClient->init($params);
        $oAuthClient->authenticate();

        return;*/
    }

    public function callbackAction() {

        $oAuthClient = Mage::getModel('restapi/api');
        $params = $oAuthClient->getConfigFromSession();
        $oAuthClient->init($params);

        $state = $oAuthClient->authenticate();

        if ($state == Vplaza_Restapi_Model_Api::OAUTH_STATE_ACCESS_TOKEN) {
            $acessToken = $oAuthClient->getAuthorizedToken();
        }

        $restClient = $acessToken->getHttpClient($params);
        // Set REST resource URL
        $restClient->setUri('http://syonserver.com/vipplaza/restapi/index/products');
        // In Magento it is neccesary to set json or xml headers in order to work
        $restClient->setHeaders('Accept', 'application/json');
        // Get method
        $restClient->setMethod(Zend_Http_Client::GET);
        //Make REST request
        $response = $restClient->request();
        // Here we can see that response body contains json list of products
        Zend_Debug::dump($response);

        return;
    }

	/**
	* User Register 
 	*/
	public function signupAction()
	{
		$websiteId = Mage::app()->getWebsite()->getId();
		$store = Mage::app()->getStore();
		
		//Get post values
		$fname = 'VIP'; //$this->getRequest()->getPost('fname');
		$lname = 'Customer'; //$this->getRequest()->getPost('lname');
		$email = $this->getRequest()->getPost('email');
		$password = $this->getRequest()->getPost('password');
		$gender = $this->getRequest()->getPost('gender');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		//Save Address
		/*$address = Mage::getModel("customer/address");
		$address->setCustomerId($customer->getId())
				->setFirstname($customer->getFirstname())
				->setMiddleName($customer->getMiddlename())
				>setLastname($customer->getLastname())
				->setCountryId('HR')
				//->setRegionId('1') //state/province, only needed if the country is USA
				->setPostcode('31000')
				->setCity('Osijek')
				->setTelephone('0038511223344')
				->setFax('0038511223355')
				->setCompany('Inchoo')
				->setStreet('Kersov')
				->setIsDefaultBilling('1')
				->setIsDefaultShipping('1')
				->setSaveInAddressBook('1');*/
				
		//Save Customer Detail
		$customer = Mage::getModel("customer/customer");
		
		$customer->setWebsiteId($websiteId)
					->setStore($store);
		
		$customer->loadByEmail($email);
		
		if ($customer->getId()) {
			$response['msg'] = 'Email Already Exist';
			$response['status'] = 0;
			echo json_encode($response);
			exit;
	    }

        $customer->setEmail($email)
					->setFirstname($fname)
					->setLastname($lname)
					->setPassword($password)
					->setGender($gender);
		
		try {
			$customer->save();

			//$address->save();
			$response['msg'] = 'New User Registered Successfully';
			$response['status'] = 1;
			$response['user_id'] = $customer->getId();
		}
		catch (Exception $e) {
			$response['msg'] = $e->getMessage();
			$response['status'] = 0;
			//Zend_Debug::dump($e->getMessage());
		}
		echo json_encode($response);
		exit;
	}
	
	/**
	* Login 
 	*/
	public function loginAction()
	{
		require_once ("app/Mage.php");
		umask(0);
		ob_start();
		session_start();
		Mage::app('default');
		Mage::getSingleton("core/session", array("name" => "frontend"));
	
		$websiteId = Mage::app()->getWebsite()->getId();
		$store = Mage::app()->getStore();
		$customer = Mage::getModel("customer/customer");
		$customer->website_id = $websiteId;
		$customer->setStore($store);
		
		$email = $this->getRequest()->getPost('email');
		$password = $this->getRequest()->getPost('password');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }
		
		$response = array();
		
		if (!empty($email) && !empty($password )) {
			try {
				$customer->loadByEmail($email);
				$session = Mage::getSingleton('customer/session')->setCustomerAsLoggedIn($customer);
				$ses = $session->login($email, $password);
				$ses = $session->getCustomer();
				$user_id = Mage::helper('customer')->getCustomer()->getData('entity_id');
				$response['msg'] = 'Login Successful';
				$response['user_id'] = $user_id;
				$response['status'] = 1;
			}catch(Exception $e){
				switch ($e->getCode()) {
					case Mage_Customer_Model_Customer::EXCEPTION_EMAIL_NOT_CONFIRMED:
						$value = Mage::helper('customer')->getEmailConfirmationUrl($email);
						$message = Mage::helper('customer')->__('This account is not confirmed. <a href="%s">Click here</a> to resend confirmation email.', $value);
						break;
					case Mage_Customer_Model_Customer::EXCEPTION_INVALID_EMAIL_OR_PASSWORD:
						$message = $e->getMessage();
						break;
					default:
						$message = $e->getMessage();
				}
				
				$response['msg'] = $message;
				$response['status'] = 0;
				//$session->setUsername($email);
			}
		}
		else
		{
			$response['msg'] = 'Email and password are required';
			$response['status'] = 0;
		}
		
		echo json_encode($response);
	}
	
	/**
	* Social Login and Register 
 	*/
	public function socialAction()
	{
		require_once ("app/Mage.php");
		umask(0);
		ob_start();
		session_start();
		Mage::app('default');
		Mage::getSingleton("core/session", array("name" => "frontend"));
		
		$websiteId = Mage::app()->getWebsite()->getId();
		$store = Mage::app()->getStore();
		
		//Get post values
		$fname = $this->getRequest()->getPost('fname');
		$lname = $this->getRequest()->getPost('lname');
		$email = $this->getRequest()->getPost('email');
		$random = substr($fname,0,4).rand(10001,999999);
		$password = $random;
		$gender = $this->getRequest()->getPost('gender');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		
		//Save Customer Detail
		$customer = Mage::getModel("customer/customer");
		
		$customer->setWebsiteId($websiteId)
					->setStore($store);
		
		$customer->loadByEmail($email);
		
		if ($customer->getId()) {
			if (!empty($email)) {
				
				$response['msg'] = 'Login Successful';
				$response['user_id'] = $customer->getId();
				$response['status'] = 1;
			}
			
			echo json_encode($response);
			exit;
	    }
		
		$customer->setEmail($email)
					->setFirstname($fname)
					->setLastname($lname)
					->setPassword($password)
					->setGender($gender);
		
		try {
			$customer->save();
			$customer->sendNewAccountEmail();

            if (!empty($email)) {
				try {
					$customer->loadByEmail($email);
					$session = Mage::getSingleton('customer/session')->setCustomerAsLoggedIn($customer);
					$ses = $session->login($email,$password);
					$ses = $session->getCustomer();
					$user_id = Mage::helper('customer')->getCustomer()->getData('entity_id');
					$response['msg'] = 'Login Successful';
					$response['user_id'] = $user_id;
					$response['status'] = 1;
				}catch(Exception $e){
					switch ($e->getCode()) {
						case Mage_Customer_Model_Customer::EXCEPTION_EMAIL_NOT_CONFIRMED:
							$value = Mage::helper('customer')->getEmailConfirmationUrl($email);
							$message = Mage::helper('customer')->__('This account is not confirmed. <a href="%s">Click here</a> to resend confirmation email.', $value);
							break;
						case Mage_Customer_Model_Customer::EXCEPTION_INVALID_EMAIL_OR_PASSWORD:
							$message = $e->getMessage();
							break;
						default:
							$message = $e->getMessage();
					}
					
					$response['msg'] = $message;
					$response['status'] = 0;
				}
			}
			
			$connectionWrite = Mage::getSingleton('core/resource')->getConnection('core_write');
			
			$connectionWrite->query("update customer_entity set social = '1' WHERE entity_id = ".$customer->getId());
			
			$response['msg'] = 'New User Registered Successfully';
			$response['status'] = 1;
			$response['user_id'] = $customer->getId();
		}
		catch (Exception $e) {
			$response['msg'] = $e->getMessage();
			$response['status'] = 0;
			//Zend_Debug::dump($e->getMessage());
		}
		echo json_encode($response);
		exit;
	}
	
	/**
	* Login User Detail 
 	*/
	public function getUserDetailAction()
	{
		$response = array();
		//By ID
		$id = $this->getRequest()->getPost('uid');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }
		$customerData = Mage::getModel('customer/customer')->load($id)->getData();
    	//Mage::log($customerData);
		if(!empty($customerData))
		{
			$response['data'] = $customerData;
			$response['msg'] = 'success';
			$response['status'] = 1;
		}
		else
		{
			$response['msg'] = 'error';
			$response['status'] = 1;	
		}
		echo json_encode($response);	
	}
	
	/**
	* User Profile 
 	*/
	public function editProfileAction()
	{
		$response = array();
		$uid = $this->getRequest()->getPost('uid');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }
		
		require_once './app/Mage.php';
		Mage::app('default');
		
		$fname = $this->getRequest()->getPost('fname');
		$lname = $this->getRequest()->getPost('lname');
		$email = $this->getRequest()->getPost('email');
		$gender = $this->getRequest()->getPost('gender');
		
		$connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');
		
		$select = $connectionRead->select()
				->from('customer_entity', array('*'))
				->where('email=?',$email);
		
		$row = $connectionRead->fetchRow($select);   //return rows
		
		if(!empty($row))
		{
			$select1 = $connectionRead->select()
				->from('customer_entity', array('*'))
				->where('email=?',$email)
				->where('entity_id =?', $uid);
			
			$row1 = $connectionRead->fetchRow($select1);   //return rows
			
			if(!empty($row1))
			{
				$connectionWrite = Mage::getSingleton('core/resource')->getConnection('core_write');
			
				//$connectionWrite->beginTransaction();
				
				$connectionWrite->query("update customer_entity_varchar set value = '".$fname."' WHERE entity_id = ".$uid. " AND attribute_id = '5'" );
				
				$connectionWrite->query("update customer_entity_varchar set value = '".$lname."' WHERE entity_id = ".$uid. " AND attribute_id = '7'" );
				
				$connectionWrite->query("update customer_entity_int set value = '".$gender."' WHERE entity_id = '".$uid."'");
				
				$response['msg'] = 'Akun informasi tersimpan';
				$response['status'] = 1;
			}
			else
			{
				$response['msg'] = 'Sudah ada email pelanggan';
				$response['status'] = 0;	
			}
			
		}
		else
		{
			$connectionWrite = Mage::getSingleton('core/resource')->getConnection('core_write');
			
			$connectionWrite->beginTransaction();
			$data = array();
			$data['email'] = $email;
			
			//customer Entity for email
			$where = $connectionWrite->quoteInto('entity_id =?', $uid);
			$update = $connectionWrite->update('customer_entity', $data, $where);
			$connectionWrite->commit();
			
			$data1 = array();
			$data1['firstname'] = $fname;
			
			//customer varchar entity for name
			$where = $connectionWrite->quoteInto('entity_id =?', $uid,'attribute_id =?', '5');
			$connectionWrite->update('customer_entity_varchar', $data1, $where);
			$connectionWrite->commit();
			
			$data2 = array();
			$data2['lastname'] = $lname;
			$where = $connectionWrite->quoteInto('entity_id =?', $uid,'attribute_id =?', '7');
			$connectionWrite->update('customer_entity_varchar', $data2, $where);
			$connectionWrite->commit();
			
			$response['msg'] = 'Akun informasi tersimpan.';
			$response['status'] = 1;
		}
		
		echo json_encode($response);
	}
	
	/**
	* Reset Password
	* Not Using
 	*/
	public function resetpassAction()
	{
		$email='neerajbwr89@gmail.com';

		$customer = Mage::getModel('customer/customer')
                    ->setWebsiteId(Mage::app()->getStore()->getWebsiteId())
                    ->loadByEmail($email);
		$customer->sendPasswordResetConfirmationEmail();
	}
	
	/**
	* Change password 
 	*/
	public function changePassAction()
	{
		/*$customerid = 285925;
		$newpassword = 123456;
		$customer = Mage::getModel('customer/customer')->load($customerid);
		$customer->setPassword($newpassword);
		$customer->save();
		exit;*/
		
		$response = array();
		$validate = 0; 
		$result = '';
		$customerid = $this->getRequest()->getPost('uid');
		$email = $this->getRequest()->getPost('email');
		$oldpassword = $this->getRequest()->getPost('oldpass');
		$newpassword = $this->getRequest()->getPost('newpass');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		$store = Mage::app()->getStore()->getId();
		
		$websiteId = Mage::getModel('core/store')->load($store)->getWebsiteId();
		try {
			 $login_customer_result = Mage::getModel('customer/customer')->setWebsiteId($websiteId)->authenticate($email, $oldpassword);
			 $validate = 1;
		}
		catch(Exception $ex) {
			 $validate = 0;
		}
		
		if($validate == 1) {
			try {
				$customer = Mage::getModel('customer/customer')->load($customerid);
				$customer->setPassword($newpassword);
				$customer->save();
				$response['msg'] = 'Your Password has been Changed Successfully';
				$response['status'] = 1;
			}
			catch(Exception $ex) {
				$response['msg'] = 'Error : '.$ex->getMessage();
				$response['status'] = 0;
			}
		}
		else {
			$response['msg'] = 'Incorrect Old Password.';
			$response['status'] = 0;
		}
		echo json_encode($response);
		exit;
	}

    public function forgotPassAction()
    {
        $email = $this->getRequest()->getPost('email');

        $token = $this->getRequest()->getPost('token');

        /*if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }*/

        $websiteId = Mage::app()->getWebsite()->getId();
        $store = Mage::app()->getStore();

        $customer = Mage::getModel("customer/customer");

        $account = Mage::getModel("customer/account");

        $customer->setWebsiteId($websiteId)
            ->setStore($store);

        $customer->loadByEmail($email);
        if ($customer->getId()) {
            $newResetPasswordLinkToken = $newResetPasswordLinkToken =  Mage::helper('customer')->generateResetPasswordLinkToken();
            $customer->changeResetPasswordLinkToken($newResetPasswordLinkToken);
            $customer->sendPasswordResetConfirmationEmail();

            $response['msg'] = 'Check your email to reset your password';
            $response['status'] = 1;
        }
        else
        {
            $response['msg'] = 'Alamat email atau password anda salah.';
            $response['status'] = 0;
        }

        /*try {
            $login_customer_result = Mage::getModel('customer/customer')->setWebsiteId($websiteId)->authenticate($email);

            $validate = 1;
        }
        catch(Exception $ex) {
            $response['msg'] = 'Error : '.$ex->getMessage();
            $response['status'] = 0;
        }*/

        echo json_encode($response);
        exit;
    }

	/**
	* Main Cateogries
 	*/
	public function categoriesAction()
	{
		$response = array();
		$Menu = $this->getLayout()->createBlock('cms/block')->setBlockId('homepage_menu')->toHtml();
		$MENU = json_decode($Menu);
		$eventCategoryIDS = array();
        foreach($MENU[1] as $ev){
			$eventCategoryIDS[]=$ev->event_category;
		}
		$idEvent = explode(',',implode(',',$eventCategoryIDS));
		$i=0;
        //$response['token'] = $this->tokenval();
        foreach($MENU[0] as $index=>$menu) {
			
			$response[$i]['id'] = $menu->ec_id;
			$response[$i]['name'] = $menu->ec_name;
			$response[$i]['path'] = $menu->ec_url;
			
			//Sub Cateogry
			/*if(in_array($menu->ec_id,$idEvent)){
				$j=0;
				$c_all = 1;
				foreach($MENU[1] as $key => $event){
					$event_ids = explode(",",$event->event_category);
					if(in_array($menu->ec_id,$event_ids)) {
						
						$category = Mage::getResourceModel('catalog/category_collection')
							->addFieldToFilter('name', $event->name)
							->getFirstItem();
						
						$response[$i][0][$j]['id'] = $category->getId();
						$response[$i][0][$j]['name'] = $event->name;
						$response[$i][0][$j]['url'] = $event->url;
						
						$j++;
						$c_all++;
					}
				}	
			}*/
			$i++;
		}

        //Mage::getStoreConfig(self::TOKEN);

        $data['token'] = $this->tokenval();
        $data['response'] = $response;

		echo json_encode($data);
	}
	
	/**
	* Category Events
 	*/
	public function catEventsAction()
	{
		require_once './app/Mage.php';
		Mage::app('default');
		
		$response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }
		
		$now            = date("Y-m-d H:i:s", Mage::getModel('core/date')->timestamp(time()));
        $dateEvent      = explode(" ", $now);
		$allEvents      = Mage::getModel('vipevent/vipevent')->getCollection()
                            ->addFieldToFilter('event_start', array('lteq' => $now ))
                            ->addFieldToFilter('event_end', array('gteq' => $now ))
                            ->setCurPage(1);
		
		$limit  = 10;
		$eventId = strtoupper($this->getRequest()->getPost('cat_name'));
		
		$eventCategory = Mage::getModel('vipevent/eventcategory')->getCollection()
						->addFieldToFilter('ec_name', array('eq' => $eventId ))
						->setCurPage(1);
		if ($eventCategory) {
			foreach ($eventCategory as $key => $val) {
				$ec_id  = $val->getEcId();
			}
			$like = "%-".$ec_id."-%";
			$allEvents ->addFieldToFilter('event_category', array('like' => $like));                
		}
		
		$entityAttr = Mage::getSingleton('core/resource')->getTableName('catalog_category_entity');
        $allEvents->getSelect()->join( array('cc'   => $entityAttr), 'cc.entity_id = main_table.category_id', array('cc.position'));
        $allEvents->setOrder('cc.position', 'ASC');
		
		if (count($allEvents) > 0 ) {
			$i=0;
			foreach ($allEvents as $key => $event) { 
                $response[$i]['category_id'] = $event->getCategoryId();
                $response[$i]['event_name'] = $event->getEventName();
                $response[$i]['event_img'] = ($event->getEventImage()  == "") ? "" : Mage::getBaseUrl('media').$event->getEventImage();
                $response[$i]['event_logo'] = ($event->getEventLogo()   == "") ? "" : Mage::getBaseUrl('media').$event->getEventLogo();
                $response[$i]['event_promo'] = ($event->getEventPromo()  == "") ? "" : $event->getEventPromo();
                $response[$i]['event_disc'] = ($event->getDiscAmount()  == "") ? "&nbsp;" : $event->getDiscAmount();
                $response[$i]['discount_info'] = ($event->getDiscInfo()    == "") ? "&nbsp;" : $event->getDiscInfo();
                $response[$i]['event_start'] = strtotime($event->getEventStart());
                $response[$i]['event_end'] = strtotime($event->getEventEnd());
                $response[$i]['category_url'] = Mage::getModel("catalog/category")->load($categoryId)->getUrl();
				//$response[$i]['event_start'] = strtotime(date("Y-m-d H:i:s", Mage::getModel('core/date')->timestamp(time())));
                //$response[$i]['event_end'] = strtotime($eventEnd);
                $response[$i]['date_diff'] = floor( ($nowEvent - $newEnd) /(60*60*24));
                if ($response[$i]['date_diff'] >= -1) 
                    $response[$i]['last_day'] = Mage::getBaseUrl('media'). DS ."wysiwyg" . DS . "last.png";

                $newEventEnd    = str_replace(" ", "-", $eventEnd); 
                $newEventEnd    = explode("-", $newEventEnd);
				
				$i++;
			}
			
			$data['msg'] = '';
			$data['status'] = 1;
		}
		else
		{
			$data['msg'] = 'No products found';
			$data['status'] = 0;
		}
		
		/*$connection = Mage::getSingleton('core/resource')->getConnection('core_read');
		
		$cat_id = $this->getRequest()->getPost('cat_id');
		
		$select = $connection->select()
			->from('event_list', array('*')) // select * from tablename or use array('id','title') selected values
			->where('event_id=?',$cat_id);   // where id =1
		
		$rowsArray = $connection->fetchAll($select); // return all rows
		
		
		if(!empty($rowsArray))
		{
			$i=0;
			foreach($rowsArray as $row)
			{
				$response[$i] = $row;
				$response[$i]['event_logo'] = 'http://www.vipplaza.co.id/media/events/'.$row['event_logo'];
				$response[$i]['event_image'] = 'http://www.vipplaza.co.id/media/events/'.$row['event_image'];
				$response[$i]['event_start'] = strtotime($row['event_start']);
				$response[$i]['event_end'] = strtotime($row['event_end']);
				
				$i++;
			}
			
			$data['status'] = 1;
			$data['msg'] = '';
		}
		else
		{
			$data['msg'] = 'No products found';
			$data['status'] = 0;
		}*/
		
		$data['data']= $response;
		
		echo json_encode($data);
	}
	
	/**
	* Category Products 
 	*/
	public function catProductsAction()
	{
		require_once './app/Mage.php';
		Mage::app('default');
		
		$connection = Mage::getSingleton('core/resource')->getConnection('core_read');
		
		$response = array();
		$catid = $this->getRequest()->getPost('cat_id');
		$price_order = $this->getRequest()->getPost('order_price');
		$name_order = $this->getRequest()->getPost('order_name');
        $click_val = $this->getRequest()->getPost('click_val');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        if($click_val == 'name')
        {
            $nextval = 'price';
            $order_name = $name_order;
            $order_price = $price_order;
        }

        if($click_val == 'price')
        {
            $nextval = 'name';
            $order_name = $price_order;
            $order_price = $name_order;

        }

        if($click_val == '')
        {
            $click_val = 'name';
            $nextval = 'price';
            $order_name = 'ASC';
            $order_price = 'ASC';
        }

        $select = $connection->select()
            ->from('event_list', array('*'))
            ->where('category_id=?', $catid);

        $rowsArray = $connection->fetchRow($select);

		$category = Mage::getModel('catalog/category')->load($catid);
		$products = Mage::getResourceModel('catalog/product_collection')
        	->setStoreId(Mage::app()->getStore()->getId())
			->addCategoryFilter($category)
			->addAttributeToSort($click_val, $order_name)
			->addAttributeToSort($nextval, $order_price);

		$productmodel = Mage::getModel('catalog/product');
		
		if(!empty($products))
		{
			$i=0;
			foreach($products as $product)
			{
				$_product = $productmodel->load($product->getId());

                $_imgSize = 250;
                $img = '';
                $img = Mage::helper('catalog/image')->init($_product, 'small_image')->constrainOnly(true)->keepFrame(false)->resize($_imgSize)->__toString();

                $response[$i]['id'] = $product->getId();
				$response[$i]['name'] = $_product->getName();
				$response[$i]['short_description'] = $_product->getShortDescription();
				$response[$i]['long_description'] = $_product->getDescription();
				$response[$i]['price'] = number_format($_product->getPrice(),0,",",".");
				$response[$i]['speprice'] = number_format($_product->getSpecialPrice(),0,",",".");
                $response[$i]['img'] = $img; // $_product->getImageUrl();
                $response[$i]['url'] = $_product->getProductUrl();

                $categoryIds = $_product->getCategoryIds();
                $category_name = "&nbsp;";
                if(count($categoryIds) ){
                    $firstCategoryId = $categoryIds[0];
                    $_category = Mage::getModel('catalog/category')->load($firstCategoryId);

                    $category_name = $_category->getName();
                }
                $response[$i]['brand'] = $category_name;

				$qty = 0;
                $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

                if ($_product->isSaleable()) {
                    if ($_product->getTypeId() == "configurable") {
                        $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                        foreach ($associated_products as $assoc){
                            $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                            $qty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                        }
                    } elseif ($_product->getTypeId() == 'grouped') {
                        $qty = $min + 1;
                    } elseif ($_product->getTypeId() == 'bundle') {
                        $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                            $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                        foreach($associated_products as $assoc) {
                            $qty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                        }
                    } else {
                        $qty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
                    }
                }

                $response[$i]['qty'] = $qty;
                $response[$i]['minqty'] = $min;

				$i++;
			}
			$data['data']= $response;
			$data['event_start'] = strtotime($rowsArray['event_start']);
			$data['event_end'] = strtotime($rowsArray['event_end']);
			$data['status'] = 1;
			$data['msg'] = '';
		}
		else
		{
			$data['msg'] = 'No products found';
			$data['status'] = 0;
		}
		
		echo json_encode($data);
	}
	
	/**
	* Product Detail 
 	*/
	public function productDetailAction()
	{
		$response = array();
		$pid = $this->getRequest()->getPost('pid');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		$_product = Mage::getModel('catalog/product')->load($pid);
		
		$response[0]['id'] = $_product->getId();
		$response[0]['name'] = $_product->getName();
		$response[0]['short_description'] = $_product->getShortDescription();
		$response[0]['long_description'] = $_product->getDescription();
		$response[0]['price'] = number_format($_product->getPrice(),0,",",".");
		$response[0]['speprice'] = number_format($_product->getSpecialPrice(),0,",",".");

        $categoryIds = $_product->getCategoryIds();
        $category_name = "&nbsp;";
        if(count($categoryIds) ){
            $firstCategoryId = $categoryIds[0];
            $_category = Mage::getModel('catalog/category')->load($firstCategoryId);

            $category_name = $_category->getName();
        }
        $response[0]['brand_name'] = $category_name;

		$originalPrice = $_product->getPrice();
		$finalPrice = $_product->getFinalPrice();
		$percentage = 0;
		if ($originalPrice > $finalPrice) {
			$percentage = ($originalPrice - $finalPrice) * 100 / $originalPrice;
		}
		
		if ($percentage) {
			$response[0]['discount'] = $percentage.'%';
		}
		
		$response[0]['weight'] = $_product->getWeight();

        $qty	= 0;
        $min	= (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

        if ($_product->isSaleable()) {
            if ($_product->getTypeId() == "configurable") {
                $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                foreach ($associated_products as $assoc){
                    $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                    $qty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                }
            } elseif ($_product->getTypeId() == 'grouped') {
                $qty = $min + 1;
            } elseif ($_product->getTypeId() == 'bundle') {
                $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                    $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                foreach($associated_products as $assoc) {
                    $qty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                }
            } else {
                $qty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
            }
        }

		$response[0]['qty'] = $qty;
        $response[0]['minqty'] = $min;
		//$response[$i]['img'] = $_product->getImageUrl();
		$response[0]['url'] = $_product->getProductUrl();
		
		if($_product->getTypeId() == "configurable"):
			$attrSetId = $_product->getAttributeSetId();
			$arrListAttr = array (
				12  => 'bra',
				16  => 'bra_us',
				11  => 'size_clothes',
				13  => 'panties_alpha',
				14  => 'panties_nume',
				17  => 'size_pants_nume',
				9   => 'size',
				15  => 'size_shoes_eu',
				10  => 'size_shoes_non_eu',
				4   => 'size'
			);
			if ( array_key_exists($attrSetId, $arrListAttr) ) :
				$attrText = $arrListAttr[$attrSetId];
				$x=0;
				foreach ($_product->getTypeInstance(true)->getUsedProducts ( null, $_product) as $simple)
				{
					 $qtyChild = Mage::getModel('cataloginventory/stock_item')->loadByProduct($simple)
										->getQty();
					 if($qtyChild > 0)
					 {
							switch ($attrSetId) {
								case 12:
									$sizeCode   = $simple->getBra();
									break;
								case 16:
									$sizeCode   = $simple->getBraUs();
									break;
								case 11:
									$sizeCode   = $simple->getSizeClothes();
									break;
								case 13:
									$sizeCode   = $simple->getPantiesAlpha();
									break;
								case 14:
									$sizeCode   = $simple->getPantiesNume();
									break;
								case 17:
									$sizeCode   = $simple->getSizePantsNume();
									break;
								case 4:
								case 9:
									$sizeCode   = $simple->getSize();
									break;
								case 15:
									$sizeCode   = $simple->getSizeShoesEu();
									break;
								case 10:
									$sizeCode   = $simple->getSizeShoesNonEu();
									break;
							}
							$size       = $simple->getAttributeText($attrText);
                            //echo $size;
							$response[0]['size'][$x] = $size;
                            $x++;
					 }
				}
			endif;
		endif;

        $_images = Mage::getModel('catalog/product')->load($_product->getId())->getMediaGalleryImages();
		
		if($_images){
			$j=0; 
			foreach($_images as $_image){ 
				/*Mage::helper('catalog/image')
                    ->init($_product, 'thumbnail', $_image->getFile())
                    //->backgroundColor(231,231,231)
                    ->resize(1000,1000)->__toString();*/
                $imgurl = Mage::getBaseUrl(Mage_Core_Model_Store::URL_TYPE_MEDIA).'/catalog/product'.$_image->getFile();
                $response[0]['img'][$j] = $imgurl; // Mage::getBaseUrl().'image_crop.php?image='.rawurldecode($imgurl).'&width=600&height=1000&cropratio=600:1000';
				$j++;
    		}
		}

        $data['data'] = $response;
		$data['msg'] = '';
		$data['status'] = 1;
		
		//echo '<pre>'; print_r($response);
		echo json_encode($response);
	}
	
	/**
	* Home Page Slider 
 	*/
	public function homesliderAction()
	{
		$response = array();
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		$response[0][0]	= 'http://www.vipplaza.co.id/media/banner/sliding-ARIES-GOLD-mobile.jpg';
		$response[0][1]	= 'http://www.vipplaza.co.id/media/banner/sliding-FRAGANCE-mobile.jpg';
		$response[0][2]	= 'http://www.vipplaza.co.id/media/banner/POLICE-SLIDING-MOBILE.jpg';
		$response[0][3]	= 'http://www.vipplaza.co.id/media/banner/ADIKUSUMA-sliding-mobile.jpg';
		
		echo json_encode($response);
		exit;
	}
	
	/**
	* Home Page Events 
 	*/
	public function homeeventsAction()
	{
		require_once './app/Mage.php';
		Mage::app('default');

        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

		$connection = Mage::getSingleton('core/resource')->getConnection('core_read');

        $now = date("Y-m-d H:i:s", Mage::getModel('core/date')->timestamp(time()));

        $dateEvent = explode(" ", $now);
        $allEvents = Mage::getModel('vipevent/vipevent')->getCollection()
            ->addFieldToFilter('event_start', array('lteq' => $now ))
            ->addFieldToFilter('event_end', array('gteq' => $now ))
            ->setCurPage(1);

        $entityAttr = Mage::getSingleton('core/resource')->getTableName('catalog_category_entity');
        $allEvents->getSelect()->join( array('cc'   => $entityAttr), 'cc.entity_id = main_table.category_id', array('cc.position'));
        $allEvents->setOrder('cc.position', 'ASC');

        if (count($allEvents) > 0 ) {
            $i=0;
            foreach ($allEvents as $key => $event) {
                $response[$i]['category_id'] = $event->getCategoryId();
                $response[$i]['event_name'] = $event->getEventName();
                $response[$i]['event_image'] = ($event->getEventImage() == "") ? "" : Mage::getBaseUrl('media') . $event->getEventImage();
                $response[$i]['event_logo'] = ($event->getEventLogo() == "") ? "" : Mage::getBaseUrl('media') . $event->getEventLogo();
                $response[$i]['event_promo'] = ($event->getEventPromo() == "") ? "" : $event->getEventPromo();
                $response[$i]['event_disc'] = ($event->getDiscAmount() == "") ? "&nbsp;" : $event->getDiscAmount();
                $response[$i]['event_disinfo'] = ($event->getDiscInfo() == "") ? "&nbsp;" : $event->getDiscInfo();
                $eventStart = $event->getEventStart();
                $eventEnd = $event->getEventEnd();
                $categoryUrl = Mage::getModel("catalog/category")->load($categoryId)->getUrl();

                $response[$i]['event_start'] = strtotime($eventEnd);
                $response[$i]['event_end'] = strtotime(date("Y-m-d H:i:s", Mage::getModel('core/date')->timestamp(time())));
                $response[$i]['current_time'] = time();
                $dateDiff = floor(($nowEvent - $newEnd) / (60 * 60 * 24));
                $eventLastDay = '';
                if ($dateDiff >= -1)
                    $response[$i]['event_lastday'] = Mage::getBaseUrl('media') . DS . "wysiwyg" . DS . "last.png";

                $i++;
            }

            $data['status'] = 1;
            $data['msg'] = '';
        }
        else
        {
            $data['msg'] = 'No products found';
            $data['status'] = 0;
        }

		$data['data']= $response;

		echo json_encode($data);
	}
	
	/**
	* Product Insert Into Cart 
 	*/
	public function addtocartAction()
	{
		require_once './app/Mage.php';
		Mage::app('default');

        $params = array();
		$uid = $this->getRequest()->getPost('uid');
        $pid = $this->getRequest()->getPost('pid');
		$pqty = $this->getRequest()->getPost('qty');
        $size = $this->getRequest()->getPost('size');
        $params['cptions']['size_clothes'] = $size;

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchRow($selectrows);

        $entity_id = $rowArray['entity_id'];

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?', $entity_id)
            ->where('product_id=?', $pid);

        $rowItems = $connectionRead->fetchAll($select);

        $qtycount = 0;
        $items = array();
        foreach ($rowItems as $item) {
            if ($item['price'] != 0) {
                $qty2 = number_format($item['qty'],0);
                $qtycount += $qty2;
            }
        }

        $_product = Mage::getModel('catalog/product')->load($pid);

        $qty = 0;
        $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

        if ($_product->isSaleable()) {
            if ($_product->getTypeId() == "configurable") {
                $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                foreach ($associated_products as $assoc){
                    $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                    $qty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                }
            } elseif ($_product->getTypeId() == 'grouped') {
                $qty = $min + 1;
            } elseif ($_product->getTypeId() == 'bundle') {
                $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                    $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                foreach($associated_products as $assoc) {
                    $qty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                }
            } else {
                $qty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
            }
        }

        $response = array();

        $checkqty = $qtycount + $pqty;

        /*if($checkqty > 2)
        {
            $response['msg'] = 'The maximum quantity allowed for purchase is 2';
            $response['status'] = '0';
            echo json_encode($response);
            exit;
        }*/

        $params['product_id'] = $pid;
        $params['qty'] = $pqty;

        if ($_product->getTypeId() == "configurable" && isset($params['cptions'])) {

            // Get configurable options
            $productAttributeOptions = $_product->getTypeInstance(true)
                ->getConfigurableAttributesAsArray($_product);

            foreach ($productAttributeOptions as $productAttribute) {
                $attributeCode = $productAttribute['attribute_code'];

                if (isset($params['cptions'][$attributeCode])) {
                    $optionValue = $params['cptions'][$attributeCode];

                    foreach ($productAttribute['values'] as $attribute) {
                        if ($optionValue == $attribute['store_label']) {
                            $params['super_attribute'] = array(
                                $productAttribute['attribute_id'] => $attribute['value_index']
                            );
                            //$params['options'][$productAttribute['attribute_id']] = $attribute['value_index'];
                        }
                    }
                }
                else{
                    foreach ($productAttribute['values'] as $attribute) {
                        if (trim($size) == $attribute['store_label']) {
                            $params['super_attribute'] = array(
                                $productAttribute['attribute_id'] => $attribute['value_index']
                            );
                        }
                    }
                }
            }
        }

        unset($params['cptions']);

        //$childProduct = Mage::getModel('catalog/product_type_configurable')->getProductByAttributes(151, $_product);

        try {

            $customerData = Mage::getModel('customer/customer')->load($uid)->getData();

            $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

            $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

            $rowArray = $connectionRead->fetchRow($selectrows);

            if(!empty($rowArray))
            {
                $entity_id = $rowArray['entity_id'];
                $db_write = Mage::getSingleton('core/resource')->getConnection('core_read');
                $sqlQuerys = "SELECT * FROM sales_flat_quote_item WHERE quote_id =".$rowArray['entity_id']." AND product_id = ".$pid." ORDER BY item_id DESC";
                $rowArrays = $db_write->fetchRow($sqlQuerys);

                if(!empty($rowArrays))
                {
                    if($rowArrays['product_type'] == 'configurable')
                    {
                        $sqlQueryss = "SELECT * FROM sales_flat_quote_item WHERE parent_item_id =".$rowArrays['item_id'];
                        $rowArrayss = $db_write->fetchRow($sqlQueryss);

                        //check Quantity count
                        //$sqlQuantity = "SELECT * FROM sales_flat_quote_item WHERE item_id =".$rowArrays['parent_item_id'];
                        //$rowArrayqnt = $db_write->fetchRow($sqlQuantity);

                        $checkqty = number_format($rowArrays['qty']) + $pqty;

                        $oldsize = explode('(', $rowArrayss['name']);
                        if(isset($oldsize[1])) {
                            $newsize = str_replace(' )', '', $oldsize[1]);
                        }
                        else{
                            $oldsize = explode('-', $rowArrayss['name']);
                            if(isset($oldsize[1]))
                            {
                                $newsize = $oldsize[1];
                            }
                            else
                            {
                                $oldsize = explode('\'', $rowArrayss['name']);
                                if(isset($oldsize[1]))
                                {
                                    $newsize = $size[1];
                                }
                            }
                        }

                        if(trim($newsize) == trim($size))
                        {
                            if($checkqty > 2)
                            {
                                $response['msg'] = 'The maximum quantity allowed for purchase is 2';
                                $response['status'] = '0';
                                echo json_encode($response);
                                exit;
                            }
                            $connections = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $date = date('Y-m-d h:i:s');
                            $totqty = $rowArrays['qty'] + $pqty;
                            $totprice = $_product->getSpecialPrice() * $totqty;
                            $connections->query("UPDATE `sales_flat_quote_item` SET `updated_at`='".$date."',`qty`=".$totqty.",`row_total`=".$totprice.",`base_row_total`=".$totprice.",`price_incl_tax`=".$totprice.",`base_price_incl_tax`=".$totprice.",`row_total_incl_tax`=".$totprice.",`base_row_total_incl_tax`=".$totprice." WHERE product_id =".$pid." AND product_type ='configurable'");

                            $rowqty = $rowArray['items_qty'] + $pqty;
                            $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $connectionquote->query("UPDATE `sales_flat_quote` SET `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                        }
                        else
                        {
                            $connectionWrit = Mage::getSingleton('core/resource')->getConnection('core_write');

                            $date = date('Y-m-d h:i:s');
                            $sku = $_product->getSku();
                            $name = $_product->getName();
                            $price = $_product->getSpecialPrice() * $pqty;
                            $connectionWrit->query("INSERT INTO `sales_flat_quote_item`(`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1','0','".$sku."','".$name."','1.0000',".$pqty.",".$price.",".$price.",".$price.",".$price.",'1.0000','configurable',".$price.",".$price.",".$price.",".$price.",'0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");

                            $db_write = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $sqlQuery = "SELECT item_id FROM sales_flat_quote_item ORDER BY item_id DESC LIMIT 1;";
                            $rowArrayes = $db_write->fetchRow($sqlQuery);
                            $item_id = $rowArrayes['item_id'];

                            $connectionWri = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $sname = $name.'( '.$size.' )';
                            $connectionWri->query("INSERT INTO `sales_flat_quote_item` (`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `parent_item_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1',".$item_id.",'0','".$sku."','".$sname."','1.0000',".$pqty.",'0.0000','0.0000','0.0000','0.0000','1.0000','simple','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");

                            $rowqty = $rowArray['items_qty'] + $pqty;
                            $rowcount = $rowArray['items_count'] + $pqty;
                            $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $connectionquote->query("UPDATE `sales_flat_quote` SET `items_count`=".$rowcount.", `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                        }
                    }

                    if($rowArrays['product_type'] == 'simple')
                    {
                        if($rowArrays['parent_item_id'] != NULL)
                        {
                            //check Quantity count
                            $sqlQueryss = "SELECT * FROM sales_flat_quote_item WHERE item_id =".$rowArrays['parent_item_id'];
                            $rowArrayss = $db_write->fetchRow($sqlQueryss);

                            $checkqty = number_format($rowArrayss['qty']) + $pqty;

                            $oldsize = explode('(', $rowArrays['name']);
                            if(isset($oldsize[1])) {
                                $newsize = str_replace(' )', '', $oldsize[1]);
                            }
                            else{
                                $oldsize = explode('-', $rowArrays['name']);
                                if(isset($oldsize[1]))
                                {
                                    $newsize = $oldsize[1];
                                }
                                else
                                {
                                    $oldsize = explode('\'', $rowArrays['name']);
                                    if(isset($oldsize[1]))
                                    {
                                        $newsize = $size[1];
                                    }
                                }
                            }

                            if(trim($newsize) == trim($size))
                            {
                                if($checkqty > 2)
                                {
                                    $response['msg'] = 'The maximum quantity allowed for purchase is 2';
                                    $response['status'] = '0';
                                    echo json_encode($response);
                                    exit;
                                }
                                $connections = Mage::getSingleton('core/resource')->getConnection('core_write');
                                $date = date('Y-m-d h:i:s');
                                $totqty = $rowArrays['qty'] + $pqty;
                                $totprice = $_product->getSpecialPrice() * $totqty;
                                $connections->query("UPDATE `sales_flat_quote_item` SET `updated_at`='".$date."',`qty`=".$totqty.",`row_total`=".$totprice.",`base_row_total`=".$totprice.",`price_incl_tax`=".$totprice.",`base_price_incl_tax`=".$totprice.",`row_total_incl_tax`=".$totprice.",`base_row_total_incl_tax`=".$totprice." WHERE product_id =".$pid." AND product_type ='configurable'");

                                $rowqty = $rowArray['items_qty'] + $pqty;
                                $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                                $connectionquote->query("UPDATE `sales_flat_quote` SET `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                            }
                            else
                            {
                                $connectionWrit = Mage::getSingleton('core/resource')->getConnection('core_write');

                                $date = date('Y-m-d h:i:s');
                                $sku = $_product->getSku();
                                $name = $_product->getName();
                                $price = $_product->getSpecialPrice() * $pqty;
                                $connectionWrit->query("INSERT INTO `sales_flat_quote_item`(`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1','0','".$sku."','".$name."','1.0000',".$pqty.",".$price.",".$price.",".$price.",".$price.",'1.0000','configurable',".$price.",".$price.",".$price.",".$price.",'0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");

                                $db_write = Mage::getSingleton('core/resource')->getConnection('core_write');
                                $sqlQuery = "SELECT item_id FROM sales_flat_quote_item ORDER BY item_id DESC LIMIT 1;";
                                $rowArrayes = $db_write->fetchRow($sqlQuery);
                                $item_id = $rowArrayes['item_id'];

                                $connectionWri = Mage::getSingleton('core/resource')->getConnection('core_write');
                                $sname = $name.'( '.$size.' )';
                                $connectionWri->query("INSERT INTO `sales_flat_quote_item` (`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `parent_item_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1',".$item_id.",'0','".$sku."','".$sname."','1.0000',".$pqty.",'0.0000','0.0000','0.0000','0.0000','1.0000','simple','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");

                                $rowqty = $rowArray['items_qty'] + $pqty;
                                $rowcount = $rowArray['items_count'] + $pqty;
                                $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                                $connectionquote->query("UPDATE `sales_flat_quote` SET `items_count`=".$rowcount.", `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                            }
                        }
                        else
                        {
                            echo 'else'; exit;
                            if($checkqty > 2)
                            {
                                $response['msg'] = 'The maximum quantity allowed for purchase is 2';
                                $response['status'] = '0';
                                echo json_encode($response);
                                exit;
                            }
                            $connections = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $date = date('Y-m-d h:i:s');
                            $totqty = $rowArrays['qty'] + $pqty;
                            $totprice = $_product->getSpecialPrice() * $totqty;
                            $connections->query("UPDATE `sales_flat_quote_item` SET `updated_at`='".$date."',`qty`=".$totqty.",`row_total`=".$totprice.",`base_row_total`=".$totprice.",`price_incl_tax`=".$totprice.",`base_price_incl_tax`=".$totprice.",`row_total_incl_tax`=".$totprice.",`base_row_total_incl_tax`=".$totprice." WHERE product_id =".$pid." AND product_type ='simple'");

                            $rowqty = $rowArray['items_qty'] + $pqty;
                            $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                            $connectionquote->query("UPDATE `sales_flat_quote` SET `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                        }

                    }
                }
                else
                {
                    if($_product->getTypeId() == "configurable")
                    {
                        $connectionWrit = Mage::getSingleton('core/resource')->getConnection('core_write');

                        $date = date('Y-m-d h:i:s');
                        $sku = $_product->getSku();
                        $name = $_product->getName();
                        $price = $_product->getSpecialPrice() * $pqty;
                        $connectionWrit->query("INSERT INTO `sales_flat_quote_item`(`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1','0','".$sku."','".$name."','1.0000',".$pqty.",".$price.",".$price.",".$price.",".$price.",'1.0000','configurable',".$price.",".$price.",".$price.",".$price.",'0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");
                        //$connectionWrit->query("UPDATE `sales_flat_quote_item` SET `quote_id`=".$rowArray['entity_id'].",`created_at`='".$date."',`updated_at`='".$date."',`product_id`=".$pid.",`store_id`=1,`parent_item_id`='',`is_virtual`=0,`sku`='".$sku."',`name`='".$name."',`description`='',`applied_rule_ids`='',`additional_data`='',`free_shipping`=0,`is_qty_decimal`=0,`no_discount`=0,`weight`='1.0000',`qty`=".$pqty.",`price`=".$price.",`base_price`=".$price.",`custom_price`='',`discount_percent`='0',`discount_amount`='0',`base_discount_amount`='0',`tax_percent`='0',`tax_amount`='0',`base_tax_amount`='0',`row_total`=".$price.",`base_row_total`=".$price.",`row_total_with_discount`='0',`row_weight`='1.0000',`product_type`='configurable',`base_tax_before_discount`='',`tax_before_discount`='',`original_custom_price`='',`redirect_url`='',`base_cost`='',`price_incl_tax`=".$price.",`base_price_incl_tax`=".$price.",`row_total_incl_tax`=".$price.",`base_row_total_incl_tax`=".$price.",`hidden_tax_amount`=0,`base_hidden_tax_amount`=0,`gift_message_id`='',`weee_tax_disposition`='0.0000',`weee_tax_row_disposition`='0.0000',`base_weee_tax_disposition`='0.0000',`base_weee_tax_row_disposition`='0.0000',`weee_tax_applied`='a:0:{}',`weee_tax_applied_amount`='0.0000',`weee_tax_applied_row_amount`='0.0000',`base_weee_tax_applied_amount`='0.0000',`base_weee_tax_applied_row_amnt`='' WHERE 1");

                        $db_write = Mage::getSingleton('core/resource')->getConnection('core_write');
                        $sqlQuery = "SELECT item_id FROM sales_flat_quote_item ORDER BY item_id DESC LIMIT 1;";
                        $rowArrayes = $db_write->fetchRow($sqlQuery);
                        $item_id = $rowArrayes['item_id'];

                        $connectionWri = Mage::getSingleton('core/resource')->getConnection('core_write');
                        $sname = $name.'( '.$size.' )';
                        $connectionWri->query("INSERT INTO `sales_flat_quote_item` (`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `parent_item_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1',".$item_id.",'0','".$sku."','".$sname."','1.0000',".$pqty.",'0.0000','0.0000','0.0000','0.0000','1.0000','simple','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");
                        //$connectionWri->query("UPDATE `sales_flat_quote_item` SET `quote_id`=".$rowArray['entity_id'].",`created_at`='".$date."',`updated_at`='".$date."',`product_id`=".$pid.",`store_id`=1,`parent_item_id`='',`is_virtual`=0,`sku`='".$sku."',`name`='".$sname."',`description`='',`applied_rule_ids`='',`additional_data`='',`free_shipping`=0,`is_qty_decimal`=0,`no_discount`=0,`weight`='1.0000',`qty`=".$pqty.",`price`='0',`base_price`='0',`custom_price`='',`discount_percent`='0',`discount_amount`='0',`base_discount_amount`='0',`tax_percent`='0',`tax_amount`='0',`base_tax_amount`='0',`row_total`='0',`base_row_total`='0',`row_total_with_discount`='0',`row_weight`='0.0000',`product_type`='simple',`base_tax_before_discount`='',`tax_before_discount`='',`original_custom_price`='',`redirect_url`='',`base_cost`='',`price_incl_tax`='',`base_price_incl_tax`='',`row_total_incl_tax`='',`base_row_total_incl_tax`='',`hidden_tax_amount`='',`base_hidden_tax_amount`='',`gift_message_id`='',`weee_tax_disposition`='0.0000',`weee_tax_row_disposition`='0.0000',`base_weee_tax_disposition`='0.0000',`base_weee_tax_row_disposition`='0.0000',`weee_tax_applied`='a:0:{}',`weee_tax_applied_amount`='0.0000',`weee_tax_applied_row_amount`='0.0000',`base_weee_tax_applied_amount`='0.0000',`base_weee_tax_applied_row_amnt`='' WHERE 1");
                    }

                    if($_product->getTypeId() == "simple")
                    {
                        $connectionWrit = Mage::getSingleton('core/resource')->getConnection('core_write');

                        $date = date('Y-m-d h:i:s');
                        $sku = $_product->getSku();
                        $name = $_product->getName();
                        $price = $_product->getSpecialPrice() * $pqty;

                        $connectionWrit->query("INSERT INTO `sales_flat_quote_item`(`quote_id`, `created_at`, `updated_at`, `product_id`, `store_id`, `is_virtual`, `sku`, `name`, `weight`, `qty`, `price`, `base_price`, `row_total`, `base_row_total`, `row_weight`, `product_type`, `price_incl_tax`, `base_price_incl_tax`, `row_total_incl_tax`, `base_row_total_incl_tax`, `weee_tax_disposition`, `weee_tax_row_disposition`, `base_weee_tax_disposition`, `base_weee_tax_row_disposition`, `weee_tax_applied`, `weee_tax_applied_amount`, `weee_tax_applied_row_amount`, `base_weee_tax_applied_amount`, `base_weee_tax_applied_row_amnt`) VALUES (".$rowArray['entity_id'].",'".$date."','".$date."',".$pid.",'1','0','".$sku."','".$name."','1.0000',".$pqty.",".$price.",".$price.",".$price.",".$price.",'1.0000','simple',".$price.",".$price.",".$price.",".$price.",'0.0000','0.0000','0.0000','0.0000','a:0:{}','0.0000','0.0000','0.0000','')");

                        //$connectionWrit->query("UPDATE `sales_flat_quote_item` SET `quote_id`=".$rowArray['entity_id'].",`created_at`='".$date."',`updated_at`='".$date."',`product_id`=".$pid.",`store_id`=1,`parent_item_id`='',`is_virtual`=0,`sku`='".$sku."',`name`='".$name."',`description`='',`applied_rule_ids`='',`additional_data`='',`free_shipping`=0,`is_qty_decimal`=0,`no_discount`=0,`weight`='1.0000',`qty`=".$pqty.",`price`=".$price.",`base_price`=".$price.",`custom_price`='',`discount_percent`='0',`discount_amount`='0',`base_discount_amount`='0',`tax_percent`='0',`tax_amount`='0',`base_tax_amount`='0',`row_total`=".$price.",`base_row_total`=".$price.",`row_total_with_discount`='0',`row_weight`='1.0000',`product_type`='configurable',`base_tax_before_discount`='',`tax_before_discount`='',`original_custom_price`='',`redirect_url`='',`base_cost`='',`price_incl_tax`=".$price.",`base_price_incl_tax`=".$price.",`row_total_incl_tax`=".$price.",`base_row_total_incl_tax`=".$price.",`hidden_tax_amount`=0,`base_hidden_tax_amount`=0,`gift_message_id`='',`weee_tax_disposition`='0.0000',`weee_tax_row_disposition`='0.0000',`base_weee_tax_disposition`='0.0000',`base_weee_tax_row_disposition`='0.0000',`weee_tax_applied`='a:0:{}',`weee_tax_applied_amount`='0.0000',`weee_tax_applied_row_amount`='0.0000',`base_weee_tax_applied_amount`='0.0000',`base_weee_tax_applied_row_amnt`='' WHERE 1");
                    }

                    $rowqty = $rowArray['items_qty'] + $pqty;
                    $rowcount = $rowArray['items_count'] + $pqty;
                    $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
                    $connectionquote->query("UPDATE `sales_flat_quote` SET `items_count`=".$rowcount.", `items_qty`=".$rowqty." WHERE `entity_id`=".$rowArray['entity_id']);
                }
            }
            else
            {
                $cart = Mage::getModel('checkout/cart');
                $cart->init();
                $cart->addProduct($_product, $params);
                $cart->save();

                $db_write = Mage::getSingleton('core/resource')->getConnection('core_read');
                $sqlQuery = "SELECT quote_id FROM sales_flat_quote_item ORDER BY item_id DESC LIMIT 1;";
                $rowArray = $db_write->fetchRow($sqlQuery);
                $entity_id = $rowArray['quote_id'];

                $connectionWrite = Mage::getSingleton('core/resource')->getConnection('core_write');

                //Update customer Id
                $date = date('Y-m-d h:i:s');
                $connectionWrite->query("update sales_flat_quote set customer_id = '".$uid."', customer_email = '".$customerData['email']."',customer_firstname = '".$customerData['firstname']."',customer_lastname = '".$customerData['lastname']."',customer_gender = '".$customerData['gender']."', updated_at = '".$date."'  WHERE entity_id = ".$entity_id);
            }

            $connectionWrites = Mage::getSingleton('core/resource')->getConnection('core_write');

            $connectionWrites->query("update sales_flat_quote_address set customer_id = '".$uid."', email = '".$customerData['email']."',firstname = '".$customerData['firstname']."',lastname = '".$customerData['lastname']."' WHERE quote_id = ".$entity_id);

            $response['msg'] = 'Product added in cart';
            $response['status'] = 1;
        }
        catch (Exception $e) {

            $response['msg'] = $e->getMessage();
            $response['status'] = 0;
        }


		echo json_encode($response);
	}
	
	/**
	* Product count in cart 
 	*/
	public function cartCountAction()
	{
		$uid = $this->getRequest()->getPost('uid');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchRow($selectrows);

        $count = number_format($rowArray['items_qty'],0);
        $entity_id = $rowArray['entity_id'];

        $response = array();

        if($count > 0) {
            $response['msg'] = '';
            $response['status'] = 1;
            $response['count'] = $count;
            $response['entity_id'] = $entity_id;
        }
        else{
            $response['msg'] = '';
            $response['status'] = 1;
            $response['count'] = 0;
            $response['entity_id'] = 0;
        }

		echo json_encode($response);
	}
	
	/**
	* Product detail from cart
 	*/
	public function cartDetailAction()
	{

        $uid = $this->getRequest()->getPost('uid');
        $entity_id = $this->getRequest()->getPost('entity_id');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        /*$selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchRow($selectrows);*/

        $response = array();

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?', $entity_id);

        $items = $connectionRead->fetchAll($select);

        $z=0;
        foreach($items as $item)
        {
            $_product = Mage::getModel('catalog/product')->load($item['product_id']);

            $_imgSize = 250;
            $img = Mage::helper('catalog/image')->init($_product, 'small_image')->constrainOnly(true)->keepFrame(false)->resize($_imgSize)->__toString();

            if ($item['price'] != 0) {
                $response[$z]['entity_id'] = $item['quote_id'];
                $response[$z]['id'] = $item['product_id'];
                $response[$z]['name'] = $item['name'];
                $response[$z]['sku'] = $item['sku'];
                $response[$z]['ssku'] = $_product->getSsku();
                $response[$z]['img'] = $img; //$_product->getImageUrl();

                $qty = 0;
                $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

                if ($_product->isSaleable()) {
                    if ($_product->getTypeId() == "configurable") {
                        $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                        foreach ($associated_products as $assoc) {
                            $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                            $qty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                        }
                    } elseif ($_product->getTypeId() == 'grouped') {
                        $qty = $min + 1;
                    } elseif ($_product->getTypeId() == 'bundle') {
                        $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                            $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                        foreach ($associated_products as $assoc) {
                            $qty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                        }
                    } else {
                        $qty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
                    }
                }

                $response[$z]['qty'] = number_format($item['qty'], 0);
                $response[$z]['totalqty'] = $qty;
                $response[$z]['price'] = number_format($item['price'], 0, '', '.');
                $response[$z]['mainprice'] = number_format($_product->getPrice(),0,",",".");
                //$response[0]['speprice'] = number_format($_product->getSpecialPrice(),0,",",".");
                $response[$z]['price_incl_tax'] = number_format($item['row_total_incl_tax'], 0, '', '.');
                $submitqty = round($item['row_total_incl_tax'] / $item['price'],0);
                $response[$z]['submitqty'] = number_format($item['qty'], 0);
                $total += $item['row_total_incl_tax'];
            } else {
                $size = explode('(', $item['name']);
                if(isset($size[1])) {
                    $response[$x]['size'] = str_replace(' )', '', $size[1]);
                }
                else{
                    $size = explode('-', $item['name']);
                    if(isset($size[1]))
                    {
                        $response[$x]['size'] = $size[1];
                    }
                    else
                    {
                        $size = explode('\'', $item['name']);
                        if(isset($size[1]))
                        {
                            $response[$x]['size'] = $size[1];
                        }
                        else
                        {
                            $response[$x]['size'] = 'M';
                        }
                    }
                }
            }
            $x = $z;
            $z++;
        }

        //print_r(array_values($response));

        $data['data'] = array_values($response);
        $data['subtotal'] = number_format($total, 0, '', '.');
        //$data['count'] = $count;
        //}
		echo json_encode($data);
	}

    public function updateCartAction()
    {
        $entity_id = $this->getRequest()->getPost('entity_id');
        $pid = $this->getRequest()->getPost('pid');
        $qty = $this->getRequest()->getPost('qty');
        $uid = $this->getRequest()->getPost('uid');
        $size = $this->getRequest()->getPost('size');
        $sku = $this->getRequest()->getPost('sku');
        $click_val = $this->getRequest()->getPost('click_val');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        /*$selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchAll($selectrows);*/

        $response = array();
        $j=0;

        $items = array();

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?', $entity_id)
            ->where('sku=?', $sku)
            ->where('parent_item_id!=?', '');

        $items = $connectionRead->fetchRow($select);

        if($size !='')
        {
            $_product = Mage::getModel('catalog/product')->load($items['product_id']);

            $stock = Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product);

            $pqty = $stock->getData('qty');
        }
        else
        {
            $_product = Mage::getModel('catalog/product')->load($pid);

            $pqty = 0;
            $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

            if ($_product->isSaleable()) {
                if ($_product->getTypeId() == "configurable") {
                    $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                    foreach ($associated_products as $assoc){
                        $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                        $pqty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                    }
                } elseif ($_product->getTypeId() == 'grouped') {
                    $pqty = $min + 1;
                } elseif ($_product->getTypeId() == 'bundle') {
                    $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                        $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                    foreach($associated_products as $assoc) {
                        $pqty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                    }
                } else {
                    $pqty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
                }
            }
        }

        if($pqty == 0)
        {
            $_product = Mage::getModel('catalog/product')->load($pid);

            $pqty = 0;
            $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

            if ($_product->isSaleable()) {
                if ($_product->getTypeId() == "configurable") {
                    $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                    foreach ($associated_products as $assoc){
                        $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                        $pqty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                    }
                } elseif ($_product->getTypeId() == 'grouped') {
                    $pqty = $min + 1;
                } elseif ($_product->getTypeId() == 'bundle') {
                    $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                        $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                    foreach($associated_products as $assoc) {
                        $pqty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                    }
                } else {
                    $pqty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
                }
            }
        }

        $item_id = number_format($items['parent_item_id'],0);

        $selectArr = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('item_id=?', $item_id);

        $rowArray = $connectionRead->fetchRow($selectArr);
        $qtycount = number_format($rowArray['qty'],0);

        $response = array();

        if($click_val == 'plus')
        {
           $checkqty = $qtycount + $qty;
        }
        else
        {
            $checkqty = $qtycount - $qty;
        }

        if($checkqty > $pqty)
        {
            $response['msg'] = 'The maximum quantity allowed for purchase is '.number_format($pqty,0);
            $response['status'] = '0';
            echo json_encode($response);
            exit;
        }

        $price = $_product->getSpecialPrice();

        $total = $price * $checkqty;

        $connection = Mage::getSingleton('core/resource')->getConnection('core_write');

        $connection->query("update sales_flat_quote_item set qty = '".$checkqty."', row_total = '".$total."', base_row_total = '".$total."',price_incl_tax = '".$total."',base_price_incl_tax = '".$total."',row_total_incl_tax = '".$total."',base_row_total_incl_tax = '".$total."'  WHERE item_id = ".$item_id);

        $connectionReads = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selects = $connectionReads->select()
            ->from('sales_flat_quote', array('*'))
            ->where('entity_id=?', $entity_id);

        $itemss = $connectionRead->fetchRow($selects);

        if($click_val == 'plus')
        {
            $rowqty = $itemss['items_qty'] + $qty;
        }
        else
        {
            $rowqty = $itemss['items_qty'] - $qty;
        }

        if($rowqty == 1)
        {
            $rowcount = 1;
        }

        $connectionwrite = Mage::getSingleton('core/resource')->getConnection('core_write');
        $connectionwrite->query("update sales_flat_quote set items_qty = '".$rowqty."', grand_total = '".$total."', base_grand_total = '".$total."', subtotal = '".$total."', base_subtotal = '".$total."' WHERE entity_id = ".$entity_id);

        //Cart Count
        $connectionReader = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectsrow = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArrays = $connectionReader->fetchRow($selectsrow);

        $count = number_format($rowArrays['items_qty'],0);
        $entity_id = $rowArrays['entity_id'];

        if($count > 0) {
            $response['count'] = $count;
            $response['entity_id'] = $entity_id;
        }
        else{
            $response['count'] = 0;
            $response['entity_id'] = 0;
        }

        $response['msg'] = 'Updated';
        $response['status'] = 1;

        echo json_encode($response);
    }

	public function removeCartProductAction()
	{
        $id = $this->getRequest()->getPost('pid'); // replace product id with your id
        $uid = $this->getRequest()->getPost('uid');
        $entity_id = $this->getRequest()->getPost('entity_id');
        $sku = $this->getRequest()->getPost('sku');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?', $entity_id)
            ->where('sku=?', $sku);

        $items = $connectionRead->fetchRow($select);

        $connectionReads = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selects = $connectionReads->select()
            ->from('sales_flat_quote', array('*'))
            ->where('entity_id=?', $entity_id);

        $itemss = $connectionRead->fetchRow($selects);

        $rowqty = $itemss['items_qty'] - $items['qty'];
        $rowcount = $itemss['items_count'] - $items['qty'];

        if($rowqty == 1)
        {
            $rowcount = 1;
        }

        $connectionquote = Mage::getSingleton('core/resource')->getConnection('core_write');
        $connectionquote->query("UPDATE `sales_flat_quote` SET `items_count`=".$rowcount.", `items_qty`=".$rowqty." WHERE `entity_id`=".$entity_id);

        $connection = Mage::getSingleton('core/resource')->getConnection('core_write');

        $__condition = array($connection->quoteInto('sku=?', $sku),$connection->quoteInto('quote_id=?', $entity_id));
        $connection->delete('sales_flat_quote_item', $__condition);

        //Cart Count
        $connectionReader = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectsrow = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArrays = $connectionReader->fetchRow($selectsrow);

        $count = number_format($rowArrays['items_qty'],0);
        $entity_id = $rowArrays['entity_id'];

        if($count > 0) {
            $response['count'] = $count;
            $response['entity_id'] = $entity_id;
        }
        else{
            $response['count'] = 0;
            $response['entity_id'] = 0;
        }

        $response['msg'] = 'product delete';
        $response['status'] = 1;

        echo json_encode($response);
    }

    public function checkAddressAction()
    {
        $uid = $this->getRequest()->getPost('uid');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $response = array();

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = "SELECT address_id,firstname,lastname,email,street,country_id,region,region_id,city,postcode,telephone,fax,address_type FROM sales_flat_quote_address WHERE customer_id =" . $uid. " ORDER BY address_id DESC LIMIT 2";

        $rowArray = $connectionRead->fetchAll($select);

        if(!empty($rowArray)) {
            if(!empty($rowArray[0]['country_id']))
            {
                $response['data'] = $rowArray;
                $response['msg'] = '';
                $response['status'] = 1;
            }
            else
            {
                $select = "SELECT cev.attribute_id,cev.entity_id,cev.value FROM `customer_address_entity` As ce LEFT JOIN `customer_address_entity_varchar` AS cev ON ce.entity_id=cev.`entity_id` WHERE ce.parent_id = ".$uid;

                $rowArrays = $connectionRead->fetchAll($select);

                $address = array();

                $address[0]['address_id'] = $rowArray[0]['address_id'];
                $address[0]['firstname'] = $rowArrays[0]['value'];
                $address[0]['lastname'] = $rowArrays[1]['value'];
                $address[0]['street'] = $rowArrays[2]['value'];
                $address[0]['city'] = $rowArrays[3]['value'];
                $address[0]['country_id'] = $rowArrays[4]['value'];
                $address[0]['region'] = $rowArrays[5]['value'];
                $address[0]['postcode'] = $rowArrays[6]['value'];
                $address[0]['telephone'] = $rowArrays[7]['value'];
                $address[0]['fax'] = $rowArrays[8]['value'];
                $address[0]['address_type'] = 'billing';

                $address[1]['address_id'] = $rowArray[1]['address_id'];
                $address[1]['firstname'] = $rowArrays[0]['value'];
                $address[1]['lastname'] = $rowArrays[1]['value'];
                $address[1]['street'] = $rowArrays[2]['value'];
                $address[1]['city'] = $rowArrays[3]['value'];
                $address[1]['country_id'] = $rowArrays[4]['value'];
                $address[1]['region'] = $rowArrays[5]['value'];
                $address[1]['postcode'] = $rowArrays[6]['value'];
                $address[1]['telephone'] = $rowArrays[7]['value'];
                $address[1]['fax'] = $rowArrays[8]['value'];
                $address[1]['address_type'] = 'shipping';

                if(!empty($address)) {
                    $response['data'] = $address;
                    $response['entity_id'] = $rowArrays[0]['entity_id'];

                    $response['msg'] = '';
                    $response['status'] = 1;
                }
                else
                {
                    $response['msg'] = '';
                    $response['status'] = 0;
                }
            }
        }
        else{

            $select = "SELECT cev.attribute_id,cev.entity_id,cev.value FROM `customer_address_entity` As ce LEFT JOIN `customer_address_entity_varchar` AS cev ON ce.entity_id=cev.`entity_id` WHERE ce.parent_id = ".$uid;

            $rowArray = $connectionRead->fetchAll($select);

            $address = array();

            if(!empty($rowArray)) {
                $address[0]['firstname'] = $rowArray[0]['value'];
                $address[0]['lastname'] = $rowArray[1]['value'];
                $address[0]['street'] = '';
                $address[0]['city'] = $rowArray[2]['value'];
                $address[0]['country_id'] = $rowArray[3]['value'];
                $address[0]['region'] = $rowArray[4]['value'];
                $address[0]['postcode'] = $rowArray[5]['value'];
                $address[0]['telephone'] = $rowArray[6]['value'];
                $address[0]['fax'] = $rowArray[7]['value'];
                $address[0]['address_type'] = 'billing';

                $response['data'] = $address;
                $response['entity_id'] = $rowArray[0]['entity_id'];
                $response['msg'] = '';
                $response['status'] = 1;
            }
            else
            {
                $response['msg'] = '';
                $response['status'] = 0;
            }
        }


        echo json_encode($response);
    }

    public function addAddressAction()
    {
        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $websiteId = Mage::app()->getWebsite()->getId();
        $store = Mage::app()->getStore();

        $address = Mage::getModel("customer/address");
        $address->setCustomerId($this->getRequest()->getPost('uid'))
            ->setFirstname($this->getRequest()->getPost('fname'))
            ->setLastname($this->getRequest()->getPost('lname'))
            ->setCountryId($this->getRequest()->getPost('country_id'))
            ->setPostcode($this->getRequest()->getPost('zip'))
            ->setRegion($this->getRequest()->getPost('state'))
            ->setRegionId($this->getRequest()->getPost('state_id'))
            ->setCity($this->getRequest()->getPost('city'))
            ->setTelephone($this->getRequest()->getPost('mobile'))
            ->setFax($this->getRequest()->getPost('othernum'))
            ->setStreet('Kersov')
            ->setIsDefaultBilling('1')
            ->setIsDefaultShipping('1')
            ->setSaveInAddressBook('1');

        try{
            $address->save();
            $response['msg'] = 'Added';
        }
        catch (Exception $e) {
            $response['msg'] = $e->getMessage();
        }

        $response['status'] = 1;

        echo json_encode($response);
        exit;
    }

    public function saveAddressAction()
    {
        $address_id = $this->getRequest()->getPost('address_id');
        $entity_id = $this->getRequest()->getPost('entity_id');
        $uid = $this->getRequest()->getPost('uid');
        $fname = $this->getRequest()->getPost('fname');
        $lname = $this->getRequest()->getPost('lname');
        $address1 = $this->getRequest()->getPost('address1');
        $address2 = $this->getRequest()->getPost('address2');
        $country_id = $this->getRequest()->getPost('country_id');
        $state = $this->getRequest()->getPost('state');
        $state_id = $this->getRequest()->getPost('state_id');
        $city = $this->getRequest()->getPost('city');
        $zip = $this->getRequest()->getPost('zip');
        $mobile = $this->getRequest()->getPost('mobile');
        $othernum = $this->getRequest()->getPost('othernum');

        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionWrites = Mage::getSingleton('core/resource')->getConnection('core_write');

        if($address_id == '') {
            if($entity_id != '') {

                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $fname . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 20" );
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $lname . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 22");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $city . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 26");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $country_id . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 27");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $state . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 28");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $zip . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 30");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $mobile . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 31");
                $connectionWrites->query("update customer_address_entity_varchar set value = '" . $othernum . "' WHERE entity_id = " . $entity_id. " AND attribute_id = 32");

            }
            else
            {
                $connectionWrites->query("update sales_flat_quote_address set firstname = '" . $fname . "',lastname = '" . $lname . "',street = '" . $address1 . ' ' . $address2 . "',country_id = '" . $country_id . "',region = '" . $state . "',region_id = '" . $state_id . "',city = '" . $city . "',postcode = '" . $zip . "',telephone = '" . $mobile . "', fax = '" . $othernum . "' WHERE customer_id = " . $uid);
            }
        }
        else
        {
            $connectionWrites->query("update sales_flat_quote_address set firstname = '" . $fname . "',lastname = '" . $lname . "',street = '" . $address1 . ' ' . $address2 . "',country_id = '" . $country_id . "',region = '" . $state . "',region_id = '" . $state_id . "',city = '" . $city . "',postcode = '" . $zip . "',telephone = '" . $mobile . "',fax = '" . $othernum . "' WHERE address_id = " . $address_id);
        }

        $response['msg'] = 'Added';
        $response['status'] = 1;

        echo json_encode($response);
    }

    public function cmsAction()
    {
        //$cmsArray = Mage::getModel('cms/page')->getCollection()->toOptionArray();

        $id = $this->getRequest()->getPost('id');
        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }
        $connection = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connection->select()
            ->from('cms_page', array('content_heading','content'))
            ->where('page_id=?', $id);

        $rowArray = $connection->fetchRow($select);

        $response['name'] = $rowArray['content_heading'];
        $response['html'] = $rowArray['content'];

        echo json_encode($response);
    }

    public function getProvinceAction()
    {

        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $countryId = Mage::helper('core')->getDefaultCountry();

        $select = $this->getLayout()->createBlock('core/html_select')
            ->setName('shipping[country_id]')
            ->setId('shipping:country_id')
            ->setTitle(Mage::helper('checkout')->__('Country'))
            ->setClass('validate-select')
            ->setValue($countryId)
            ->setOptions($this->getCountryOptions());

        $response = $select->getOptions();

        unset($response[0]);

        echo json_encode($response);
        exit;
    }

    public function getCountryOptions()
    {
        $options    = false;
        $useCache   = Mage::app()->useCache('config');
        if ($useCache) {
            $cacheId    = 'DIRECTORY_COUNTRY_SELECT_STORE_' . Mage::app()->getStore()->getCode();
            $cacheTags  = array('config');
            if ($optionsCache = Mage::app()->loadCache($cacheId)) {
                $options = unserialize($optionsCache);
            }
        }

        if ($options == false) {
            $options = $this->getCountryCollection()->toOptionArray();
            if ($useCache) {
                Mage::app()->saveCache(serialize($options), $cacheId, $cacheTags);
            }
        }
        return $options;
    }

    public function getCountryCollection()
    {
        if (!$this->_countryCollection) {
            $this->_countryCollection = Mage::getSingleton('directory/country')->getResourceCollection()
                ->loadByStore();
        }
        return $this->_countryCollection;
    }

    public function getStateAction()
    {
        /*$countryList = Mage::getResourceModel('directory/country_collection')
            ->loadData()
            ->toOptionArray(false);

        echo '<pre>';
        print_r( $countryList);
        exit();*/

        $country_id = $this->getRequest()->getPost('country_id');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        //$states = Mage::getModel('directory/country')->load($country_id)->getRegions();

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('citylist', array('*'))
            ->where('country_code=?',$country_id)
            ->group('region_name');

        $states = $connectionRead->fetchAll($select);

        //state names
        $response = array();

        $i=0;
        foreach ($states as $state) {
            $response[$i]['id'] = $state['id_citylist'];
            $response[$i]['country_code'] = $state['country_code'];
            $response[$i]['name'] = $state['region_name'];
            $i++;
        }

        echo json_encode($response);
        exit;
    }

    public function getCityAction()
    {
        $state_id = $this->getRequest()->getPost('region_name');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('citylist', array('*'))
            ->where('region_name=?',$state_id);

        $rowArray = $connectionRead->fetchAll($select);

        $i=0;
        foreach($rowArray as $row)
        {
            $response[$i]['id'] = $row['id_citylist'];
            $response[$i]['city_name'] = $row['city_name'];
            $i++;
        }

        echo json_encode($response);
        exit;
    }

    public function shippingAction()
    {
        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('sales_flat_quote_shipping_rate', array('code','method','method_title','price'))
            ->group('carrier');

        $rowArray = $connectionRead->fetchAll($select);

        //$rowArray[value] = 'freeshipping';
        //$rowArray[label] = 'Promo Pengiriman (freeshipping)';

        $response['data'] = $rowArray;
        $response['msg'] = '';
        $response['status'] = 1;

        echo json_encode($response);

        /*$methods = Mage::getSingleton('shipping/config')->getActiveCarriers();

        foreach($methods as $_code => $_method)
        {
            if(!$_title = Mage::getStoreConfig("carriers/$_code/title"))
                $_title = $_code;

            $options[] = array('value' => $_code, 'label' => $_title . " ($_code)");

            print_r($options);
        }*/
    }

    public function paymentMethodsAction()
    {
        $payments = Mage::getSingleton('payment/config')->getActiveMethods();

        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $i=0;
        foreach ($payments as $paymentCode=>$paymentModel) {

            //$paymentid = Mage::getStoreConfig('payment/'.$paymentCode.'/id');

            $paymentTitle = Mage::getStoreConfig('payment/'.$paymentCode.'/title');

            $paymentHtml = Mage::getStoreConfig('payment/'.$paymentCode.'/instructions');

            $html = $this->getLayout()->createBlock('cms/block')->setBlockId($paymentHtml)->toHtml();

            $response[$i] = array(
                //'id'     => $paymentid,
                'label'   => $paymentTitle,
                'value' => $paymentCode,
                'html'  => $html
            );


            $i++;
        }

        $data['data'] = $response;
        $data['msg'] = '';
        $data['status'] = 1;

        echo json_encode($data);
    }

    public function getOrderHistoryAction()
    {
        $uid = $this->getRequest()->getPost('uid');

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $collection = Mage::getModel('sales/order')
            ->getCollection()
            ->addAttributeToFilter('customer_id',$uid)
            ->setOrder('created_at','DESC');

        $response = array();

        $i=0;
        foreach($collection as $order){

            $response[$i] = array(
                'id' => $order->getId(),
                'order_id' => $order->getRealOrderId(),
                'date'     => date('Y-m-d',strtotime($order->getCreatedAt())),
                'name'     => $order->getShippingAddress()->getName(),
                'total'    => number_format($order->getGrandTotal(),0,',','.'),
                'status'   => $order->getStatusLabel()
            );
            $i++;
        }

        $data['data'] = $response;
        $data['msg'] = '';
        $data['status'] = 1;

        echo json_encode($data);
    }

    public function orderHistoryDetailAction()
    {
        $order_id = $this->getRequest()->getPost('order_id');

        $orderId = Mage::getModel('sales/order')->loadByIncrementId($order_id)->getEntityId();

        $order = Mage::getModel("sales/order")->load($orderId);

        $response = array();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $orderItems = $order->getItemsCollection();
        $i=0;

        foreach ($orderItems as $item){

            if($item->getPrice() != 0)
            {
                $response[$i]['product_id'] = $item->product_id;
                $response[$i]['product_sku'] = $item->sku;
                $response[$i]['product_price'] = number_format($item->getPrice(),0,',','.');
                $response[$i]['product_name'] = $item->getName();
                $qty = $item->getQtyOrdered();
                $price = $item->getPrice();
                $subtotal += intval($qty) * intval($price);
                $response[$i]['qty'] = intval($item->getQtyOrdered());
                $response[$i]['status'] = $item->getStatus();
                $net = intval($qty) * intval($price);
                $response[$i]['subtot'] = number_format($net,0,'','.');

                $_product = Mage::getModel('catalog/product')->load($item->product_id);
                $cats = $_product->getCategoryIds();
                $category_id = $cats[0];
                $category = Mage::getModel('catalog/category')->load($category_id);
                $response[$i]['category_name'] = $category->getName();

                $x = $i;
                $i++;
            }
            else {
                $size = explode('(', $item['name']);
                if(isset($size[1])) {
                    $response[$x]['size'] = str_replace(' )', '', $size[1]);
                }
                else{
                    $size = explode('-', $item['name']);
                    $response[$x]['size'] = $size[1];
                }
            }
        }

        $data['data'][] = (array)$response;

        $data['order_date'] = date('Y-m-d',strtotime($order->getCreatedAtStoreDate()));
        $data['subtotal'] = number_format($subtotal, 0, ',', '.');
        $data['totalprice'] = number_format($order->getGrandTotal(), 0, ',', '.');

        if($data['subtotal'] != $data['totalprice'])
        {
            $data['discount'] = $data['subtotal'] - $data['totalprice'];
        }

        $data['shipping_method'] = $order->getShippingDescription();
        $paymentCode = $order->getPayment()->getMethodInstance()->getCode();
        $data['payment_method_code'] = Mage::getStoreConfig('payment/'.$paymentCode.'/title');
        $data['delivery_price'] = 0;
        $data['shipping_address'] = $order->getShippingAddress()->format('html');
        $data['billing_address'] = $order->getBillingAddress()->format('html');

        $data['msg'] = '';
        $data['status'] = 1;

        echo json_encode($data);

    }

    public function contactInfoAction()
    {
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $contactinfo = '<p>Jl. KH Mohamad Mansyur No. 14</p>
                    <p>Kel. Duri Pulo Kec. Gambir</p>
                    <p>Jakarta Pusat 10140</p>';

        $customersupport = '<p><a href="tel:+62216320880">+ 62 (21)632-0880</a></p>
                    <p>Senin - Jumat : 09.00 - 18:00 WIB</p>
                    <p>Sabtu : 09.00 - 12.00 WIB</p>';

        $social = array(
            'facebook' => array(
                'name' => 'vipplaza',
                'url' => 'http://www.facebook.com/vipplaza',
            ),
            'twitter' => array(
                'name' => '@vipplazaid',
                'url' => 'http://www.twitter.com/vipplazaid',
            ),
            'instagram' => array(
                'name' => '@vipplazaid',
                'url' => 'http://www.instagram.com/vipplazaid',
            )
        );

        $email_address = array(
            'email' => array('brands@vipplaza.co.id','marketing@vipplaza.co.id','hrd@vipplaza.co.id','cs@vipplaza.co.id')
        );

        $response[0]['contactinfo'] = $contactinfo;
        $response[0]['customersupport'] = $customersupport;
        $response[0]['social'] = $social;
        $response[0]['email_address'] = $email_address;

        echo json_encode($response);
    }

    public function contactMailAction()
    {
        $post = $this->getRequest()->getPost();

        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $translate = Mage::getSingleton('core/translate');
        /* @var $translate Mage_Core_Model_Translate */
        $translate->setTranslateInline(false);

        $response = array();

        try {
            $postObject = new Varien_Object();
            $postObject->setData($post);

            $mailTemplate = Mage::getModel('core/email_template');
            /* @var $mailTemplate Mage_Core_Model_Email_Template */
            $mailTemplate->setDesignConfig(array('area' => 'frontend'))
                ->setReplyTo($post['email'])
                ->sendTransactional(
                    Mage::getStoreConfig(self::XML_PATH_EMAIL_TEMPLATE),
                    Mage::getStoreConfig(self::XML_PATH_EMAIL_SENDER),
                    Mage::getStoreConfig(self::XML_PATH_EMAIL_RECIPIENT),
                    null,
                    array('data' => $postObject)
                );
            if (!$mailTemplate->getSentSuccess()) {
                throw new Exception();
            }

            $translate->setTranslateInline(true);

            $response['success'] = 1;
            $response['msg'] = 'Your inquiry was submitted and will be responded to as soon as possible. Thank you for contacting us.';

        } catch (Exception $e) {
            $translate->setTranslateInline(true);

            $response['success'] = 0;
            $response['msg'] = 'Unable to submit your request. Please, try again later';

        }

        echo json_encode($response);
    }

    public function checkoutProcessAction()
    {
        $uid = $this->getRequest()->getPost('uid');
        $entity_id = $this->getRequest()->getPost('entity_id');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        /*$selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1' AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchRow($selectrows);*/

        $response = array();

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?', $entity_id);

        $items = $connectionRead->fetchAll($select);

        $z=0;
        foreach($items as $item)
        {
            $_product = Mage::getModel('catalog/product')->load($item['product_id']);

            $_imgSize = 250;
            $img = Mage::helper('catalog/image')->init($_product, 'small_image')->constrainOnly(true)->keepFrame(false)->resize($_imgSize)->__toString();

            if ($item['price'] != 0) {
                $response[$z]['entity_id'] = $item['quote_id'];
                $response[$z]['id'] = $item['product_id'];
                $response[$z]['name'] = $item['name'];
                $response[$z]['sku'] = $item['sku'];
                $response[$z]['ssku'] = $_product->getSsku();
                $response[$z]['img'] = $img; //$_product->getImageUrl();

                $qty = 0;
                $min = (float)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getNotifyStockQty();

                if ($_product->isSaleable()) {
                    if ($_product->getTypeId() == "configurable") {
                        $associated_products = $_product->loadByAttribute('sku', $_product->getSku())->getTypeInstance()->getUsedProducts();
                        foreach ($associated_products as $assoc) {
                            $assocProduct = Mage::getModel('catalog/product')->load($assoc->getId());
                            $qty += (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($assocProduct)->getQty();
                        }
                    } elseif ($_product->getTypeId() == 'grouped') {
                        $qty = $min + 1;
                    } elseif ($_product->getTypeId() == 'bundle') {
                        $associated_products = $_product->getTypeInstance(true)->getSelectionsCollection(
                            $_product->getTypeInstance(true)->getOptionsIds($_product), $_product);
                        foreach ($associated_products as $assoc) {
                            $qty += Mage::getModel('cataloginventory/stock_item')->loadByProduct($assoc)->getQty();
                        }
                    } else {
                        $qty = (int)Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getQty();
                    }
                }

                $response[$z]['qty'] = number_format($item['qty'], 0);
                $response[$z]['totalqty'] = $qty;
                $response[$z]['price'] = number_format($item['price'], 0, '', '.');
                $response[$z]['mainprice'] = number_format($_product->getPrice(),0,",",".");
                //$response[0]['speprice'] = number_format($_product->getSpecialPrice(),0,",",".");
                $response[$z]['price_incl_tax'] = number_format($item['row_total_incl_tax'], 0, '', '.');
                $submitqty = round($item['row_total_incl_tax'] / $item['price'],0);
                $response[$z]['submitqty'] = number_format($item['qty'], 0);
                $total += $item['row_total_incl_tax'];
            } else {
                $size = explode('(', $item['name']);
                if(isset($size[1])) {
                    $response[$x]['size'] = str_replace(' )', '', $size[1]);
                }
                else{
                    $size = explode('-', $item['name']);
                    if(isset($size[1]))
                    {
                        $response[$x]['size'] = $size[1];
                    }
                    else
                    {
                        $size = explode('\'', $item['name']);
                        if(isset($size[1]))
                        {
                            $response[$x]['size'] = $size[1];
                        }
                        else
                        {
                            $response[$x]['size'] = 'M';
                        }
                    }
                }
            }
            $x = $z;
            $z++;
        }

        $data['data'] = array_values($response);
        $data['subtotal'] = number_format($total, 0, '', '.');
        $discount = ($total*15)/100;
        $data['discount'] = $discount;
        $data['totalprice'] = $total - $discount;
        echo json_encode($data);

    }

    public function checkAvailibilityAction()
    {
        $skus = $this->getRequest()->getPost('skus');
        $uid = $this->getRequest()->getPost('uid');
        $token = $this->getRequest()->getPost('token');

        if($this->tokenval() != $token)
        {
            $response['msg'] = 'You are not authorized to access this';
            $response['status'] = '1';
            echo json_encode($response);
            exit;
        }

        $sku = explode(',',$skus);
        if(isset($sku) && !empty($sku))
        {
            for($x=0;$x<count($sku);$x++)
            {
                //$product = Mage::getModel('catalog/product')->loadByAttribute('sku', $sku[$x]);

                $product_id = Mage::getModel("catalog/product")->getIdBySku($sku[$x]);

                $_product = Mage::getModel('catalog/product')->load($product_id);

                $stock = Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product);

                if($stock->getData('qty') > 0)
                {
                    $response[$sku[$x]]['msg'] = '';
                    $response[$sku[$x]]['status'] = 1;
                }
                else {
                    $response[$sku[$x]]['msg'] = 'Out of stock';
                    $response[$sku[$x]]['status'] = 0;
                }
            }
        }

        echo json_encode($response);
        exit;
    }


    public function finalpaymentAction()
    {
        $uid = $this->getRequest()->getPost('uid');
        $finaldata = $this->getRequest()->getPost('finaldata');

        $prArr = json_decode($finaldata);

        $customerData = Mage::getModel('customer/customer')->load($uid)->getData();

        $finalArr = array();
        $finalArr[0]['email'] = $customerData['email'];

        $i=1;
        foreach($prArr as $pr)
        {
            $_product = Mage::getModel('catalog/product')->load($pr->product_id);
            $params['cptions']['size_clothes'] = $pr->size;
            if ($_product->getTypeId() == "configurable" && isset($params['cptions'])) {
                // Get configurable options
                $productAttributeOptions = $_product->getTypeInstance(true)
                    ->getConfigurableAttributesAsArray($_product);

                foreach ($productAttributeOptions as $productAttribute) {
                    $attributeCode = $productAttribute['attribute_code'];

                    if (isset($params['cptions'][$attributeCode])) {
                        $optionValue = $params['cptions'][$attributeCode];

                        foreach ($productAttribute['values'] as $attribute) {
                            if ($optionValue == $attribute['store_label']) {
                                $params['super_attribute'] = array(
                                    $productAttribute['attribute_id'] => $attribute['value_index']
                                );
                            }
                        }
                    }
                    else{
                        foreach ($productAttribute['values'] as $attribute) {

                            if (trim($pr->size) == $attribute['store_label']) {

                                $params['super_attribute'] = array(
                                    $productAttribute['attribute_id'] => $attribute['value_index']
                                );
                            }
                        }
                    }
                }

                $childProduct = Mage::getModel('catalog/product_type_configurable')->getProductByAttributes($params['super_attribute'], $_product);

                $finalArr[$i]['product_id'] = $childProduct->getData('entity_id');
                $finalArr[$i]['qty'] = $pr->qty;
            }
            else{
                $finalArr[$i]['product_id'] = $pr->product_id;
                $finalArr[$i]['qty'] = $pr->qty;
            }

            $i++;
        }

        $client = new SoapClient(Mage::getBaseUrl().'api/?wsdl=1'); //replace "www.yourownaddressurl.com" with your own merchant URL
        //$client = new SoapClient('http://dev.vipplaza.co.id/index.php/api/?wsdl'); //replace "www.yourownaddressurl.com" with your own merchant URL
        $session = $client->login('tester', 'hendra123'); // U:mobileapp_skylark P:mobileapp_skylark_123 replace with username, password you have created on Magento Admin - SOAP/XML-RPC - Users
        //$arr = array(array('email'=>'a@a.com'),array('product_id'=>138006,'qty'=>1),array('product_id'=>125648,'qty'=>1));
        $arr = $finalArr;

        $param = json_encode($arr);

        $result = $client->call($session, 'icubeaddtocart.geturl', $param);
		
        echo json_encode($result);
    }
}