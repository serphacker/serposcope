    
<?php
$to      = 'mr.stachu@gmail.com';
$subject = 'the subject';
$message = 'helloS�Ə����';
$headers = 'From: poczta@hyzne.com' . "\r\n" .
    'Reply-To: mr.stachu@gmail.com' . "\r\n" .
    'X-Mailer: PHP/' . phpversion();

mail($to, $subject, $message, $headers);
?>
