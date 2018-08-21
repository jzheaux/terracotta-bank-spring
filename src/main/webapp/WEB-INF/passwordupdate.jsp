<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>

	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=Edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="keywords" content="">
	<meta name="description" content="">

	<title>Terracotta Bank</title>
<!--

Template 2080 Minimax

http://www.tooplate.com/view/2080-minimax

-->
	<!-- stylesheet css -->
	<link rel="stylesheet" href="css/bootstrap.min.css">
	<link rel="stylesheet" href="css/font-awesome.min.css">
	<link rel="stylesheet" href="css/nivo-lightbox.css">
	<link rel="stylesheet" href="css/nivo_themes/default/default.css">
	<link rel="stylesheet" href="css/style.css">
	<!-- google web font css -->
	<link href='http://fonts.googleapis.com/css?family=Raleway:400,300,600,700' rel='stylesheet' type='text/css'>
</head>
<body data-page-context="${pageContext.request.contextPath}" data-spy="scroll" data-target=".navbar-collapse" data-csrf-token="${csrfToken}">

<!-- navigation -->
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<button class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
				<span class="icon icon-bar"></span>
				<span class="icon icon-bar"></span>
				<span class="icon icon-bar"></span>
			</button>
			<a href="#home" class="navbar-brand smoothScroll">Terracotta</a>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav navbar-right">
				<c:choose>
					<c:when test="${empty authenticatedUser}">
						<li><a href="#home" class="smoothScroll">HOME</a></li>
						<li><a href="#login" class="smoothScroll">LOGIN</a></li>
					</c:when>
					<c:otherwise>
						<li><a href="#service" class="smoothScroll">HOME</a></li>
						<li><a href="${pageContext.request.contextPath}/logout">LOGOUT</a></li>
					</c:otherwise>
				</c:choose>
				<li><a href="#contact" class="smoothScroll">CONTACT</a></li>
				<li><a href="#about" class="smoothScroll">ABOUT</a></li>
			</ul>
		</div>
	</div>
</div>

<!-- account section -->
<div id="account">
    <div class="container">
        <div class="row">
            <div class="col-md-12 col-sm-12">
                <h2>Account</h2>
            </div>
            <div class="col-md-6 col-sm-6">
                <i class="fa fa-group"></i>
                <h3>Change Your Password</h3>
                <p>Make sure it's a good one</p>
                <form id="change" action="#" method="post" role="form">
                    <div class="col-md-12 col-sm-12 messages"></div>
                    <div class="col-md-6 col-sm-6">
                        <input name="oldPassword" type="password" class="form-control" id="oldPassword" placeholder="Old Password">
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <input name="changePassword" type="password" class="form-control" id="password" placeholder="Password">
                    </div>
                    <div class="col-md-6 col-sm-6">
                        <input name="verifyChangePassword" type="password" class="form-control" id="verifyPassword" placeholder="Verify Password">
                    </div>
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <div class="col-md-6 col-sm-6"></div>
                    <div class="col-md-6 col-sm-6">
                        <input name="change" type="submit" class="form-control" value="CHANGE">
                    </div>
                </form>
            </div>
            <div class="col-md-6 col-sm-6"/>
        </div>
    </div>
</div>


<!-- divider section -->
<div class="container">
    <div class="row">
        <div class="col-md-1 col-sm-1"></div>
        <div class="col-md-10 col-sm-10">
            <hr>
        </div>
        <div class="col-md-1 col-sm-1"></div>
    </div>
</div>

<!-- contact section -->
<div id="contact">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>Keep in touch</h2>
			</div>
			<form action="#" method="post" role="form">
				<div class="col-md-1 col-sm-1"></div>
				<div class="col-md-10 col-sm-10">
					<div class="col-md-12 col-sm-12 messages"></div>
					<div class="col-md-6 col-sm-6">
						<input name="contactName" type="text"
							class="form-control" id="contactName" placeholder="Name">
				  	</div>
					<div class="col-md-6 col-sm-6">
						<input name="contactEmail" type="email"
							class="form-control" id="contactEmail" placeholder="Email">
				  	</div>
                    <div class="col-md-12 col-sm-12">
						<input name="contactSubject" type="text"
							class="form-control" id="contactSubject" placeholder="Subject">
	    	  	  	</div>
					<div class="col-md-12 col-sm-12">
						<textarea name="contactMessage" rows="5"
							class="form-control" id="contactMessage" placeholder="Message"></textarea>
					</div>
					<div class="col-md-8 col-sm-8">
						<p>Questions, comments? We will send a response within 24 hours.</p>
					</div>
					<div class="col-md-4 col-sm-4">
						<input name="submit" type="submit" class="form-control" id="submit" value="SEND MESSAGE">
					</div>
				</div>
				<div class="col-md-1 col-sm-1"></div>
			</form>
		</div>
	</div>
</div>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- about section -->
<div id="about">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>Terracotta Story</h2>
			</div>
			<div class="col-md-6 col-sm-6">
				<img src="images/about-img.jpg" class="img-responsive" alt="about img">
			</div>
			<div class="col-md-6 col-sm-6">
				<h3>ABOUT US</h3>
				<h4>Security-Minded Software Engineers</h4>
				<p>This website is an experiment similar in nature to Damn-Vulnerable Web Application in PHP. Feel free to poke around to find a dearth of both famous and infamous security vulnerabilities.</p>
				<p>How many can you spot? Are you able to somehow exploit the security vulnerabilities to get secret information about the company, steal funds, or simply deface or DoS the site? Can you successfully close the vulnerabilities?</p>
			</div>
		</div>
	</div>
</div>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- team section -->
<div id="team">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<h2>People behind the project</h2>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team1.jpg" class="img-responsive" alt="team img">
				<h3>Josh Cummings</h3>
				<h4>Software Engineer</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team2.jpg" class="img-responsive" alt="team img">
				<h3>Wink Martindale </h3>
				<h4>Tech Evangelist</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team3.jpg" class="img-responsive" alt="team img">
				<h3>Happy Gilmore</h3>
				<h4>Golfer</h4>
			</div>
			<div class="col-md-3 col-sm-6">
				<img src="images/team4.jpg" class="img-responsive" alt="team img">
				<h3>Kristi Cummings</h3>
				<h4>Supportive Wife</h4>
			</div>
		</div>
	</div>
</div>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- footer section -->
<footer>
	<div class="container">
		<div class="row">
			<div class="col-md-6 col-sm-6">
				<h2>Our Office</h2>
				<p>101 Terracotta Row, San Francisco, CA 10110</p>
				<p>Email: <span>vases@terracottabank.com</span></p>
				<p>Phone: <span>010-020-0340</span></p>
			</div>
			<div class="col-md-6 col-sm-6">
				<h2>Social Us</h2>
				<ul class="social-icons">
					<li><a href="#" class="fa fa-facebook"></a></li>
					<li><a href="#" class="fa fa-twitter"></a></li>
                    <li><a href="#" class="fa fa-google-plus"></a></li>
					<li><a href="#" class="fa fa-dribbble"></a></li>
				</ul>
			</div>
		</div>
	</div>
</footer>

<!-- divider section -->
<div class="container">
	<div class="row">
		<div class="col-md-1 col-sm-1"></div>
		<div class="col-md-10 col-sm-10">
			<hr>
		</div>
		<div class="col-md-1 col-sm-1"></div>
	</div>
</div>

<!-- copyright section -->
<div class="copyright">
	<div class="container">
		<div class="row">
			<div class="col-md-12 col-sm-12">
				<p>Copyright &copy; 2016 Minimax Digital Firm

                - Design: <a rel="nofollow" href="http://www.tooplate.com" target="_parent">Tooplate</a></p>
			</div>
		</div>
	</div>
</div>

<!-- scrolltop section -->
<a href="#top" class="go-top"><i class="fa fa-angle-up"></i></a>


<!-- javascript js -->
<script src="https://code.jquery.com/jquery-3.1.1.js"
			  integrity="sha256-16cdPddA6VdVInumRGo6IbivbERE8p7CQR3HzTBuELA="
			  crossorigin="anonymous"></script>
<script src="js/bootstrap.min.js"></script>
<script src="js/nivo-lightbox.min.js"></script>
<script src="js/smoothscroll.js"></script>
<script src="js/jquery.nav.js"></script>
<script src="js/isotope.js"></script>
<script src="js/imagesloaded.min.js"></script>
<script src="js/custom.js"></script>
<script src="js/forms.js"></script>
</body>
</html>