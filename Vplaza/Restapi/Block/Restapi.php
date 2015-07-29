<?php
class Vplaza_Restapi_Block_Restapi extends Mage_Core_Block_Template
{
	public function getDate()
	{
		echo date('Y-m-d');
	}
}