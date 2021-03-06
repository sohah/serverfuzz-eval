# serverless-lambda
A sample project about S3+Lambda+DynamoDB(or RDS)

# Introduction
The project is aiming to build a serverless project with several AWS components including S3, Lambda and DynamoDB and RDS.

The background of the project is to upload the contracts with unique contract code(aka. filename). Once uploaded, DynamoDB(or RDS) will capture the upload event and record the contract code, upload date and S3 file path of the file for further filtering.

# Prerequisite
## Create the IAM policy and role to authorize the lambda function

IAM Policy Json </br>
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "s3:*",
                "logs:*",
                "dynamodb:*",
                "rds:*"
            ],
            "Resource": "*"
        }
    ]
}
```

Once setup the IAM policy, you have to attach the policy to a new IAM role or existing one.

# DynamoDB
## 1. Create the DynamoDB "Contracts" to proceed the experiment. 
You could execute the class "amazon.aws.lambda.dynamodb.ContractsDynamoDBInit" to create the DynamnoDB table. I have setup BJS as the default region to proceed the experiment. If you would like to execute it in another region, please modify the endpoint and region in class "amazon.aws.lambda.dynamodb.DynamoDBClient"

## 2. Create the S3 bucket to hold your contract files.
In this experiment, I also create a subfolder named "contract" as prefix. Please be reminded that you are supposed to change the source code if you modify the folder hierarchy in the S3 bucket.

## 3. Create a Lambda function and attach the role with IAM policy created above
https://docs.aws.amazon.com/zh_cn/toolkit-for-eclipse/v1/user-guide/lambda-tutorial.html

## 4. Use maven to pacakge the source code and upload to the Lambda function you created in step 3
https://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-no-ide.html
https://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-and-eclipse.htmlhttps://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-and-eclipse.html

## 5. Then you could upload the contract files to S3 bucket to see the results.

# RDS
## 1. Create the mysql table "contract" to proceed the experiment. 
Please create the mysql table contract
```
CREATE TABLE `contract` (
  `contract_id` varchar(255) NOT NULL COMMENT '??????ID',
  `contract_num` varchar(255) NOT NULL COMMENT '?????????',
  `client_name` varchar(255) DEFAULT NULL COMMENT '????????????',
  `client_mobile` varchar(32) DEFAULT NULL COMMENT '????????????',
  `client_num` varchar(255) DEFAULT NULL COMMENT '????????????',
  `capital` varchar(255) DEFAULT NULL COMMENT '????????????',
  `contract_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '????????????',
  `contract_name` varchar(255) DEFAULT NULL COMMENT '???????????????',
  `directory` varchar(255) DEFAULT NULL COMMENT '?????????',
  `sign_date` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '??????????????????',
  `identity_card_num` varchar(255) DEFAULT NULL COMMENT '????????????',
  `s3_bucket` varchar(255) DEFAULT NULL COMMENT 'Amazon S3 ??????',
  `s3_key` varchar(255) DEFAULT NULL COMMENT 'Amazon S3 ???',
  `create_time` datetime NOT NULL COMMENT '????????????',
  `update_time` datetime NOT NULL COMMENT '?????????????????????',
  `operator` varchar(255) DEFAULT NULL COMMENT '?????????',
  `del` tinyint(4) NOT NULL DEFAULT '0' COMMENT '????????????',
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `idx_contractnum` (`contract_num`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

## 2. Create the S3 bucket to hold your contract files.
In this experiment, I also create a subfolder named "contract" as prefix. Please be reminded that you are supposed to change the source code if you modify the folder hierarchy in the S3 bucket.

## 3. Fill in the required arguments of the DB connection in druid.properties

## 4. Create a Lambda function and attach the role with IAM policy created above
https://docs.aws.amazon.com/zh_cn/toolkit-for-eclipse/v1/user-guide/lambda-tutorial.html

## 5. Use maven to pacakge the source code and upload to the Lambda function you created in step 3
https://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-no-ide.html
https://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-and-eclipse.htmlhttps://docs.aws.amazon.com/zh_cn/lambda/latest/dg/java-create-jar-pkg-maven-and-eclipse.html

## 6. Then you could upload the contract files to S3 bucket to see the results.