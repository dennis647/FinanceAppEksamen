Kandidatnr: 2022

Password for mySql database "toor"

Needed to create a new project for spring boot:

https://github.com/dennis647/springboot-financeAppExam

If needed to create tables yourself, these are the create statements:

CREATE TABLE `users` (
`user_id` int(11) NOT NULL AUTO_INCREMENT,
`fullname` varchar(100) NOT NULL,
`savings_goal` decimal(10,2) NOT NULL DEFAULT 0.00,
PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `user_savings` (
`savings_id` int(11) NOT NULL AUTO_INCREMENT,
`user_id` int(11) NOT NULL,
`month` date DEFAULT NULL,
`amount_saved` decimal(10,2) NOT NULL,
PRIMARY KEY (`savings_id`),
KEY `user_id` (`user_id`),
CONSTRAINT `user_savings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `income` (
`income_id` int(11) NOT NULL AUTO_INCREMENT,
`user_id` int(11) DEFAULT NULL,
`month` date DEFAULT NULL,
`work_income` decimal(10,2) DEFAULT NULL,
`extra_income` decimal(10,2) DEFAULT NULL,
PRIMARY KEY (`income_id`),
KEY `user_id` (`user_id`),
CONSTRAINT `income_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `expenses` (
`expense_id` int(11) NOT NULL AUTO_INCREMENT,
`user_id` int(11) DEFAULT NULL,
`month` date DEFAULT NULL,
`category` varchar(50) DEFAULT NULL,
`amount` decimal(10,2) DEFAULT NULL,
PRIMARY KEY (`expense_id`),
KEY `user_id` (`user_id`),
CONSTRAINT `expenses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

