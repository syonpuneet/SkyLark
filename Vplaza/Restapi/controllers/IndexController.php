<?php
class Vplaza_Restapi_IndexController extends Mage_Core_Controller_Front_Action{

    public function indexAction() {

        //Basic parameters that need to be provided for oAuth authentication
        //on Magento
        $params = array(
            'siteUrl' => 'http://magento.loc/oauth',
            'requestTokenUrl' => 'http://magento.loc/oauth/initiate',
            'accessTokenUrl' => 'http://magento.loc/oauth/token',
            'authorizeUrl' => 'http://magento.loc/admin/oAuth_authorize', //This URL is used only if we authenticate as Admin user type
            'consumerKey' => 'h7n8qjybu78cc3n8cdd5dr7ujtl33uqh', //Consumer key registered in server administration
            'consumerSecret' => '2smfjx37a6e4w23jlcrya6iyv5v32fxr', //Consumer secret registered in server administration
            'callbackUrl' => 'http://magento.loc/restapi/index/callback', //Url of callback action below
        );


        $oAuthClient = Mage::getModel('vplaza_restapi/oauth_client');
        $oAuthClient->reset();

        $oAuthClient->init($params);
        $oAuthClient->authenticate();

        return;
    }

    public function callbackAction() {

        $oAuthClient = Mage::getModel('vplaza_restapi/oauth_client');
        $params = $oAuthClient->getConfigFromSession();
        $oAuthClient->init($params);

        $state = $oAuthClient->authenticate();

        if ($state == Vplaza_Restapi_Model_OAuth_Client::OAUTH_STATE_ACCESS_TOKEN) {
            $acessToken = $oAuthClient->getAuthorizedToken();
        }

        $restClient = $acessToken->getHttpClient($params);
        // Set REST resource URL
        $restClient->setUri('http://magento.loc/api/rest/products');
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
		
		//Save Address
		/*$address = Mage::getModel("customer/address");
		$address->setCustomerId($customer->getId())
				->setFirstname($customer->getFirstname())
				->setMiddleName($customer->getMiddlename())
				->setLastname($customer->getLastname())
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
		
		echo json_encode($response);
	}
	
	/**
	* Category Events
 	*/
	public function catEventsAction()
	{
		require_once './app/Mage.php';
		Mage::app('default');
		
		$response = array();
		
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
		$order_price = $this->getRequest()->getPost('order_price');
		$order_name = $this->getRequest()->getPost('order_name');

        $select = $connection->select()
            ->from('event_list', array('*'))
            ->where('category_id=?', $catid);

        $rowsArray = $connection->fetchRow($select);

		$category = Mage::getModel('catalog/category')->load($catid);
		$products = Mage::getResourceModel('catalog/product_collection')
        	->setStoreId(Mage::app()->getStore()->getId())
			->addCategoryFilter($category)
			->addAttributeToSort('name', $order_name)
			->addAttributeToSort('price', $order_price);

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
        $pid = 65699; //$this->getRequest()->getPost('pid');
		$pqty = $this->getRequest()->getPost('qty');
        $params['cptions']['size_clothes'] = $this->getRequest()->getPost('size');


        $_product = Mage::getModel('catalog/product')->load($pid);


        /*$ids=Mage::getResourceSingleton('catalog/product_type_configurable')->getChildrenIds($pid);


        $product = Mage::getModel('catalog/product')->load($ids['65696']);

        print_r($ids);

        print_r($product->getProduct()->getName()); exit;

        $_subproducts = Mage::getModel('catalog/product')->getCollection()->addIdFilter($ids)->addAttributeToSelect('size_clothes')->groupByAttribute('size_clothes');

        print_r($_subproducts); exit;

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1'  AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchAll($selectrows);

        echo $sku = $_product->getSku();

        $response = array();
        $j=0;
        //if(!empty($entity_id)) {
        foreach ($rowArray as $row) {
            $select = $connectionRead->select()
                ->from('sales_flat_quote_item', array('*'))
                ->where('quote_id=?', $row['entity_id'])
                ->where('sku=?',$sku);

            $item = $connectionRead->fetchAll($select);

            print_r($item);
        }*/


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

        /*if($qty < $pqty)
        {
            $response['msg'] = 'You can not purchase more than '.$qty.' products'.
            $response['status'] = '0';
            echo json_encode($response);
            exit;
        }
        if($min > $pqty)
        {
            $response['msg'] = 'Minimum quantity for this product '.$min.
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
            }
        }

        unset($params['cptions']);

        //$childProduct = Mage::getModel('catalog/product_type_configurable')->getProductByAttributes(151, $_product);

//print_r($params);

        //print_r($childProduct); exit;

        try {
            $_product = Mage::getModel('catalog/product')->load($pid);

            //$inStock = Mage::getModel('cataloginventory/stock_item')->loadByProduct($_product)->getIsInStock();

            $cart = Mage::getModel('checkout/cart');
            $cart->init();
            $cart->addProduct($_product, $params);
            $cart->save();

            $db_write = Mage::getSingleton('core/resource')->getConnection('core_read');
            $sqlQuery = "SELECT quote_id FROM sales_flat_quote_item ORDER BY item_id DESC LIMIT 1;";
            $rowArray = $db_write->fetchRow($sqlQuery);
            $entity_id = $rowArray['quote_id'];

            $customerData = Mage::getModel('customer/customer')->load($uid)->getData();

            $connectionWrite = Mage::getSingleton('core/resource')->getConnection('core_write');

            //Update customer Id
            $date = date('Y-m-d h:i:s');
            $connectionWrite->query("update sales_flat_quote set customer_id = '".$uid."', customer_email = '".$customerData['email']."',customer_firstname = '".$customerData['firstname']."',customer_lastname = '".$customerData['lastname']."',customer_gender = '".$customerData['gender']."', updated_at = '".$date."'  WHERE entity_id = ".$entity_id);

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

        //$items = Mage::getSingleton('checkout/session')->getQuote()->getAllItems();

        //Check product and userid available in cart table
		$connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1'  AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchAll($selectrows);

        $total = 0;
        $i=1;
        foreach ($rowArray as $row) {
            $select = $connectionRead->select()
                ->from('sales_flat_quote_item', array('*'))
                ->where('quote_id=?', $row['entity_id']);

            $item = $connectionRead->fetchRow($select);

            if(!empty($item))
            {
                $total += $item['qty'];
            }
        }

        if($total > 0) {
            $response['msg'] = '';
            $response['status'] = 1;
            $response['count'] = $total;
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
        
		$connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $selectrows = "SELECT `sales_flat_quote`.* FROM `sales_flat_quote` WHERE customer_id= ".$uid." AND is_active = '1'  AND COALESCE(reserved_order_id, '') = ''";

        $rowArray = $connectionRead->fetchAll($selectrows);
       
        $response = array();
        $j=0;
        //if(!empty($entity_id)) {
            foreach ($rowArray as $row) {
                $select = $connectionRead->select()
                    ->from('sales_flat_quote_item', array('*'))
                    ->where('quote_id=?', $row['entity_id']);

                $item = $connectionRead->fetchRow($select);

                $_product = Mage::getModel('catalog/product')->load($item['product_id']);

                $_imgSize = 250;
                $img = Mage::helper('catalog/image')->init($_product, 'small_image')->constrainOnly(true)->keepFrame(false)->resize($_imgSize)->__toString();

                if(!empty($item)) {
                    if ($item['price'] != 0) {
                        $response[$j]['entity_id'] = $row['entity_id'];
                        $response[$j]['id'] = $item['product_id'];
                        $response[$j]['name'] = $item['name'];
                        $response[$j]['sku'] = $item['sku'];
                        $response[$j]['ssku'] = $_product->getSsku();
                        $response[$j]['img'] = $img; //$_product->getImageUrl();

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

                        $response[$j]['qty'] = number_format($item['qty'], 0);
                        $response[$j]['totalqty'] = $qty;
                        $response[$j]['price'] = number_format($item['price'], 0, '', '.');
                        $response[$j]['mainprice'] = number_format($_product->getPrice(),0,",",".");
                        //$response[0]['speprice'] = number_format($_product->getSpecialPrice(),0,",",".");
                        $response[$j]['price_incl_tax'] = number_format($item['row_total_incl_tax'], 0, '', '.');
                        $total += $item['row_total_incl_tax'];
                    } else {
                        $size = explode('(', $item['name']);
                        if(isset($size[1])) {
                            $response[$x]['size'] = str_replace(' )', '', $size[1]);
                        }
                        else{
                            $size = explode('-', $item['name']);
                            $response[$x]['size'] = $size[1];
                        }
                    }
                    $x = $j;
                    $j++;
                }
            }

            $data['data'] = $response;
            $data['subtotal'] = number_format($total, 0, '', '.');
        //}
		echo json_encode($data);
	}

    public function updateCartAction()
    {
        $entity_id = $this->getRequest()->getPost('entity_id');
        $pid = $this->getRequest()->getPost('pid');
        $qty = $this->getRequest()->getPost('qty');

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('sales_flat_quote_item', array('*'))
            ->where('quote_id=?',$entity_id)
            ->where('product_id=?',$pid);

        $rowArray = $connectionRead->fetchRow($select);

        $price = $rowArray['price'];

        $total = $price * $qty;

        $connection = Mage::getSingleton('core/resource')->getConnection('core_write');

        $connection->query("update sales_flat_quote_item set qty = '".$qty."', row_total = '".$total."', base_row_total = '".$total."',price_incl_tax = '".$total."',base_price_incl_tax = '".$total."',row_total_incl_tax = '".$total."',base_row_total_incl_tax = '".$total."'  WHERE quote_id = ".$entity_id." AND product_id =".$pid);

        $response['msg'] = 'Updated';
        $response['status'] = 1;

        echo json_encode($response);
    }

	public function removeCartProductAction()
	{
        $id = $this->getRequest()->getPost('pid'); // replace product id with your id
        $entity_id = $this->getRequest()->getPost('entity_id');

        $connection = Mage::getSingleton('core/resource')->getConnection('core_write');

        $__condition = array($connection->quoteInto('quote_id=?', $entity_id));
        $connection->delete('sales_flat_quote_item', $__condition);

        //Mage::getSingleton('checkout/cart')->removeItem($id)->save();
        $response['msg'] = 'product delete';
        $response['status'] = 1;

        echo json_encode($response);
    }

    public function checkAddressAction()
    {
        $uid = $this->getRequest()->getPost('uid');

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

        $connection = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connection->select()
            ->from('cms_page', array('content_heading','content'))
            ->where('page_id=?', $id);

        $rowArray = $connection->fetchRow($select);

        $response = array();
        $response['name'] = $rowArray['content_heading'];
        $response['html'] = $rowArray['content'];

        /*$select = $connection->select()
            ->from('cms_page', array('content'))
            ->where('page_id=?', 9);

        $rowArrays = $connection->fetchRow($select);

        $response[1]['name'] = 'Syarat & Ketentuan';
        $response[1]['url'] = $rowArrays['content'];

        $response[2]['name'] = 'Cara Pemesanan';
        $response[2]['url'] = $rowArrays['content'];

        $response[3]['name'] = 'Panduan Ukuran';
        $response[3]['url'] = $rowArrays['content'];

        $response[4]['name'] = 'Pengembalian';
        $response[4]['url'] = $rowArrays['content'];

        $response[5]['name'] = 'Kebijakan';
        $response[5]['url'] = Mage::getBaseUrl().'privacy';

        $response[6]['name'] = 'Tentang Kami';
        $response[6]['url'] = Mage::getBaseUrl().'about';*/

        //$data['data'] = $response;

        echo json_encode($response);
    }

    public function getProvinceAction()
    {

        $response = array();

        $response[0]['country_code'] = 'BD';
        $response[0]['country_name'] = 'Bali';

        $response[1]['country_code'] = 'BB';
        $response[1]['country_name'] = 'Bangka Belitung';

        $response[2]['country_code'] = 'BT';
        $response[2]['country_name'] = 'Banten';

        $response[3]['country_code'] = 'BE';
        $response[3]['country_name'] = 'Bengkulu';

        $response[4]['country_code'] = 'DK';
        $response[4]['country_name'] = 'Di Yogyakarta';

        $response[5]['country_code'] = 'DJ';
        $response[5]['country_name'] = 'DKI Jakarta';

        $response[6]['country_code'] = 'GT';
        $response[6]['country_name'] = 'Gorontalo';

        $response[7]['country_code'] = 'JM';
        $response[7]['country_name'] = 'Jambi';

        $response[8]['country_code'] = 'JE';
        $response[8]['country_name'] = 'Jawa Barat';

        $response[9]['country_code'] = 'JP';
        $response[9]['country_name'] = 'Jawa Tengah';

        $response[10]['country_code'] = 'JO';
        $response[10]['country_name'] = 'Jawa Timur';

        $response[11]['country_code'] = 'KE';
        $response[11]['country_name'] = 'Kalimantan Barat';

        $response[12]['country_code'] = 'KI';
        $response[12]['country_name'] = 'Kalimantan Selatan';

        $response[13]['country_code'] = 'KM';
        $response[13]['country_name'] = 'Kalimantan Tengah';

        $response[14]['country_code'] = 'KR';
        $response[14]['country_name'] = 'Kalimantan Timur';

        $response[15]['country_code'] = 'KZ';
        $response[15]['country_name'] = 'Kepulauan Riau';

        $response[16]['country_code'] = 'LA';
        $response[16]['country_name'] = 'Lampung';

        $response[17]['country_code'] = 'MK';
        $response[17]['country_name'] = 'Maluku';

        $response[18]['country_code'] = 'MU';
        $response[18]['country_name'] = 'Maluku Utara';

        $response[19]['country_code'] = 'NA';
        $response[19]['country_name'] = 'Nanggroe Aceh Darussalam (Nad)';

        $response[20]['country_code'] = 'NC';
        $response[20]['country_name'] = 'Nusa Tenggara Barat (Ntb)';

        $response[21]['country_code'] = 'NU';
        $response[21]['country_name'] = 'Nusa Tenggara Timur (Ntt)';

        $response[22]['country_code'] = 'PA';
        $response[22]['country_name'] = 'Papua';

        $response[23]['country_code'] = 'PT';
        $response[23]['country_name'] = 'Papua Barat';

        $response[24]['country_code'] = 'RE';
        $response[24]['country_name'] = 'Riau';

        $response[25]['country_code'] = 'SB';
        $response[25]['country_name'] = 'Sulawesi Barat';

        $response[26]['country_code'] = 'SG';
        $response[26]['country_name'] = 'Sulawesi Selatan';

        $response[27]['country_code'] = 'SE';
        $response[27]['country_name'] = 'Sulawesi Tengah';

        $response[28]['country_code'] = 'ST';
        $response[28]['country_name'] = 'Sulawesi Tenggara';

        $response[29]['country_code'] = 'SO';
        $response[29]['country_name'] = 'Sulawesi Utara';

        $response[30]['country_code'] = 'SR';
        $response[30]['country_name'] = 'Sumatera Barat';

        $response[31]['country_code'] = 'SL';
        $response[31]['country_name'] = 'Sumatera Selatan';

        $response[32]['country_code'] = 'SV';
        $response[32]['country_name'] = 'Sumatera Utara';


        echo json_encode($response);
        exit;
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

        $connectionRead = Mage::getSingleton('core/resource')->getConnection('core_read');

        $select = $connectionRead->select()
            ->from('sales_flat_quote_shipping_rate', array('code','method','method_title','price'))
            ->group('carrier');

        $rowArray = $connectionRead->fetchAll($select);

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
        }*/
    }

    public function paymentMethodsAction()
    {
        $payments = Mage::getSingleton('payment/config')->getActiveMethods();

        $response = array();

        $i=0;
        foreach ($payments as $paymentCode=>$paymentModel) {

            $paymentTitle = Mage::getStoreConfig('payment/'.$paymentCode.'/title');

            $paymentHtml = Mage::getStoreConfig('payment/'.$paymentCode.'/instructions');

            $html = $this->getLayout()->createBlock('cms/block')->setBlockId($paymentHtml)->toHtml();

            $response[$i] = array(
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
}
