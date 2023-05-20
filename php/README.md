# PHP Geohexa

## Setting up PHP on Ubuntu 20.04

1. Update the package list and install necessary dependencies:

```bash
sudo apt update
sudo apt install -y software-properties-common
```

2. Add the ondrej/php repository, which provides the latest PHP versions:

```bash
sudo add-apt-repository ppa:ondrej/php
sudo apt update
```

3. Install PHP 7.4 (or any other desired version) and some common extensions:

```bash
sudo apt install -y php7.4 php7.4-cli php7.4-curl php7.4-mbstring php7.4-xml
```

4. Verify the PHP installation:

```bash
php -v
.
PHP 7.4.33 (cli) (built: Feb 14 2023 18:31:23) ( NTS )
Copyright (c) The PHP Group
Zend Engine v3.4.0, Copyright (c) Zend Technologies
    with Zend OPcache v7.4.33, Copyright (c), by Zend Technologies
```

## Setting up Geohexa and running tests

1. Install Composer:

```bash
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer
```

2. Install PHPUnit and generate the `vendor` directory and `autoload.php` file:

```bash
cd geohexa/php
composer install
```

3. Run the tests:

```bash
./vendor/bin/phpunit GeohexaTest.php
```

## Development notes

In PHP you need to use a workaround to get console logs to appear during the running of unit tests:

```php
fwrite(STDERR, "num: $num\n");
```
