<?php
include('../inc/functions.php');
include('../inc/simple_html_dom.php');
            $url = "http://www.ask.com/web?q=rzeszow";
            $doc = file_get_html($url);
                $allh3 = $doc->getElementsByTagName("h3");
                foreach($allh3 as $h3){
                    if($h3->getAttribute("class") == "wresult"){
 echo $h3;
                        try {
                            $h3_a=$h3->childNodes()->getElementsByTagName("a");

                            if($h3_a == null || $h3_a->length == 0){
                                continue;
                            }
                            $href = $h3_a->item(0)->getAttribute('href');
                            $parsed = @parse_url($href);
                            if($parsed !== FALSE && isset($parsed['host'])){
                                
                                foreach ($group['sites'] as $keySite => $website) {
                                    
                                    // if we already have a rank for this keyword, continue
                                    if(isset($ranks[$keyKW][$keySite]))
                                        continue;
                                    
                                    // wildcard support
                                    $regex = wd_wildcard_to_preg($website);
                                    
                                    if(preg_match($regex, $parsed['host'])){
                                        $ranks[$keyKW][$keySite][0]= $pos;
                                        $ranks[$keyKW][$keySite][1]= $href;
                                        
                                        $this->l("Rank[$pos] [$website] ".$href."\r\n");
                                    }
                                }                                
                                $pos++;
                            }
                        }catch(Exception $e){
                            $this->e("Parsing error (unexpected bug)");
                        }
                    }
                }
                
?>