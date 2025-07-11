package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.R

// Sample data class for icons
data class CategoryIconItem(
    val id: Int,
    val name: String,
    val category: String,
    val resourceId: Int)

// Sample icons categorized
val icons = listOf(
    //Beverages
    CategoryIconItem(1, "Beer", "Beverages", R.drawable.type_beverages_beer),
    CategoryIconItem(2, "BeverageBox Drink", "Beverages", R.drawable.type_beverages_beverage_box),
    CategoryIconItem(3, "Champagne", "Beverages", R.drawable.type_beverages_bottle_with_popping_cork),
    CategoryIconItem(4, "Bubble Tea", "Beverages", R.drawable.type_beverages_bubble_tea),
    CategoryIconItem(5, "Beers", "Beverages", R.drawable.type_beverages_clinking_beer_mugs),
    CategoryIconItem(6, "Champagnes", "Beverages", R.drawable.type_beverages_clinking_glasses),
    CategoryIconItem(7, "Cocktail", "Beverages", R.drawable.type_beverages_cocktail_glass),
    CategoryIconItem(8, "Coffee", "Beverages", R.drawable.type_beverages_coffee),
    CategoryIconItem(9, "Sake", "Beverages", R.drawable.type_beverages_sake),
    CategoryIconItem(10, "Tea", "Beverages", R.drawable.type_beverages_tea),
    CategoryIconItem(11, "Teapot", "Beverages", R.drawable.type_beverages_teapot),
    CategoryIconItem(12, "Tropical Drink", "Beverages", R.drawable.type_beverages_tropical_drink),
    CategoryIconItem(13, "Wine", "Beverages", R.drawable.type_beverages_wine_glass),
    CategoryIconItem(14, "Soft Drink", "Beverages", R.drawable.type_beverages_beverages),

    // Food
    CategoryIconItem(15, "Bacon", "Food", R.drawable.type_food_bacon),
    CategoryIconItem(16, "Bento", "Food", R.drawable.type_food_bento_box),
    CategoryIconItem(17, "Burrito", "Food", R.drawable.type_food_burrito),
    CategoryIconItem(18, "Canned Food", "Food", R.drawable.type_food_canned_food),
    CategoryIconItem(19, "Cooked Rice", "Food", R.drawable.type_food_cooked_rice),
    CategoryIconItem(20, "Cooking", "Food", R.drawable.type_food_cooking),
    CategoryIconItem(21, "Curry Rice", "Food", R.drawable.type_food_curry_rice),
    CategoryIconItem(22, "Dinning", "Food", R.drawable.type_food_dining),
    CategoryIconItem(23, "Fondue", "Food", R.drawable.type_food_fondue),
    CategoryIconItem(24, "Fried Shrimp", "Food", R.drawable.type_food_fried_shrimp),
    CategoryIconItem(25, "Green Salad", "Food", R.drawable.type_food_green_salad),
    CategoryIconItem(26, "Hamburger", "Food", R.drawable.type_food_hamburger),
    CategoryIconItem(27, "HotDog", "Food", R.drawable.type_food_hot_dog),
    CategoryIconItem(28, "Meat", "Food", R.drawable.type_food_meat_on_bone),
    CategoryIconItem(29, "Oyster", "Food", R.drawable.type_food_oyster),
    CategoryIconItem(30, "Pizza", "Food", R.drawable.type_food_pizza),
    CategoryIconItem(31, "FoodPot", "Food", R.drawable.type_food_pot_of_food),
    CategoryIconItem(32, "Poultry Leg", "Food", R.drawable.type_food_poultry_leg),
    CategoryIconItem(33, "Ramen", "Food", R.drawable.type_food_ramen),
    CategoryIconItem(34, "Rice Ball", "Food", R.drawable.type_food_rice_ball),
    CategoryIconItem(35, "Sandwich", "Food", R.drawable.type_food_sandwich),
    CategoryIconItem(36, "Pan Food", "Food", R.drawable.type_food_shallow_pan_of_food),
    CategoryIconItem(37, "spaghetti", "Food", R.drawable.type_food_spaghetti),
    CategoryIconItem(38, "Stuffed Flatbread", "Food", R.drawable.type_food_stuffed_flatbread),
    CategoryIconItem(39, "Sushi", "Food", R.drawable.type_food_sushi),
    CategoryIconItem(40, "Taco", "Food", R.drawable.type_food_taco),
    CategoryIconItem(41, "Takeout", "Food", R.drawable.type_food_takeout),

    //Fruits
    CategoryIconItem(42, "Avocado", "Fruit", R.drawable.type_fruit_avocado),
    CategoryIconItem(43, "Banana", "Fruit", R.drawable.type_fruit_banana),
    CategoryIconItem(44, "Blueberry", "Fruit", R.drawable.type_fruit_blueberries),
    CategoryIconItem(45, "Cherries", "Fruit", R.drawable.type_fruit_cherries),
    CategoryIconItem(46, "Chestnut", "Fruit", R.drawable.type_fruit_chestnut),
    CategoryIconItem(47, "Coconut", "Fruit", R.drawable.type_fruit_coconut),
    CategoryIconItem(48, "Corns", "Fruit", R.drawable.type_fruit_ear_of_corn),
    CategoryIconItem(49, "Grapes", "Fruit", R.drawable.type_fruit_grapes),
    CategoryIconItem(50, "Green Apple", "Fruit", R.drawable.type_fruit_green_apple),
    CategoryIconItem(51, "Kiwi", "Fruit", R.drawable.type_fruit_kiwi_fruit),
    CategoryIconItem(52, "Lemon", "Fruit", R.drawable.type_fruit_lemon),
    CategoryIconItem(53, "Lime", "Fruit", R.drawable.type_fruit_lime),
    CategoryIconItem(54, "Mango", "Fruit", R.drawable.type_fruit_mango),
    CategoryIconItem(55, "Melon", "Fruit", R.drawable.type_fruit_melon),
    CategoryIconItem(56, "Olive", "Fruit", R.drawable.type_fruit_olive),
    CategoryIconItem(57, "Peach", "Fruit", R.drawable.type_fruit_peach),
    CategoryIconItem(58, "Pear", "Fruit", R.drawable.type_fruit_pear),
    CategoryIconItem(59, "Pineapple", "Fruit", R.drawable.type_fruit_pineapple),
    CategoryIconItem(60, "Red Apple", "Fruit", R.drawable.type_fruit_red_apple),
    CategoryIconItem(61, "Strawberry", "Fruit", R.drawable.type_fruit_strawberry),
    CategoryIconItem(62, "Tangerine", "Fruit", R.drawable.type_fruit_tangerine),
    CategoryIconItem(63, "Tomato", "Fruit", R.drawable.type_fruit_tomato),

    // Snacks
    CategoryIconItem(64, "Cookie", "Snacks", R.drawable.type_snack_cookie),
    CategoryIconItem(65, "Dumpling", "Snacks", R.drawable.type_snack_dumpling),
    CategoryIconItem(66, "FishCake", "Snacks", R.drawable.type_snack_fish_cake_with_swirl),
    CategoryIconItem(67, "Fortune Cookie", "Snacks", R.drawable.type_snack_fortune_cookie),
    CategoryIconItem(68, "French Fries", "Snacks", R.drawable.type_snack_french_fries),
    CategoryIconItem(69, "Peanuts", "Snacks", R.drawable.type_snack_peanuts),
    CategoryIconItem(70, "Popcorn", "Snacks", R.drawable.type_snack_popcorn),
    CategoryIconItem(71, "Rice Cracker", "Snacks", R.drawable.type_snack_rice_cracker),

    // Sweets
    CategoryIconItem(72, "Bagel", "Sweets", R.drawable.type_sweet_bagel),
    CategoryIconItem(73, "Cake", "Sweets", R.drawable.type_sweet_birthday_cake),
    CategoryIconItem(74, "Candy", "Sweets", R.drawable.type_sweet_candy),
    CategoryIconItem(75, "ChocolateBar", "Sweets", R.drawable.type_sweet_chocolate_bar),
    CategoryIconItem(76, "Croissant", "Sweets", R.drawable.type_sweet_croissant),
    CategoryIconItem(77, "CupCake", "Sweets", R.drawable.type_sweet_cupcake),
    CategoryIconItem(78, "Custard", "Sweets", R.drawable.type_sweet_custard),
    CategoryIconItem(79, "Dango", "Sweets", R.drawable.type_sweet_dango),
    CategoryIconItem(80, "Doughnut", "Sweets", R.drawable.type_sweet_doughnut),
    CategoryIconItem(81, "Falafel", "Sweets", R.drawable.type_sweet_falafel),
    CategoryIconItem(82, "HoneyPot", "Sweets", R.drawable.type_sweet_honey_pot),
    CategoryIconItem(83, "IceCream", "Sweets", R.drawable.type_sweet_ice_cream),
    CategoryIconItem(84, "Lollipop", "Sweets", R.drawable.type_sweet_lollipop),
    CategoryIconItem(85, "Oden", "Sweets", R.drawable.type_sweet_oden),
    CategoryIconItem(86, "Pancake", "Sweets", R.drawable.type_sweet_pancakes),
    CategoryIconItem(87, "Pie", "Sweets", R.drawable.type_sweet_pie),
    CategoryIconItem(88, "Sweet Potato", "Sweets", R.drawable.type_sweet_roasted_sweet_potato),
    CategoryIconItem(89, "Shave Ice", "Sweets", R.drawable.type_sweet_shaved_ice),
    CategoryIconItem(90, "ShortCake", "Sweets", R.drawable.type_sweet_shortcake),
    CategoryIconItem(91, "Soft IceCream", "Sweets", R.drawable.type_sweet_soft_ice_cream),
    CategoryIconItem(92, "Waffle", "Sweets", R.drawable.type_sweet_waffle),

    // Vegetables
    CategoryIconItem(93, "Beans", "Vegetables", R.drawable.type_vegetable_beans),
    CategoryIconItem(94, "Bell Pepper", "Vegetables", R.drawable.type_vegetable_bell_pepper),
    CategoryIconItem(95, "Broccoli", "Vegetables", R.drawable.type_vegetable_broccoli),
    CategoryIconItem(96, "Brown Mushroom", "Vegetables", R.drawable.type_vegetable_brown_mushroom),
    CategoryIconItem(97, "Carrot", "Vegetables", R.drawable.type_vegetable_carrot),
    CategoryIconItem(98, "Cucumber", "Vegetables", R.drawable.type_vegetable_cucumber),
    CategoryIconItem(99, "Eggplant", "Vegetables", R.drawable.type_vegetable_eggplant),
    CategoryIconItem(100, "Garlic", "Vegetables", R.drawable.type_vegetable_garlic),
    CategoryIconItem(101, "Ginger", "Vegetables", R.drawable.type_vegetable_ginger_root),
    CategoryIconItem(102, "Hot Pepper", "Vegetables", R.drawable.type_vegetable_hot_pepper),
    CategoryIconItem(103, "Mushroom", "Vegetables", R.drawable.type_vegetable_mushroom),
    CategoryIconItem(104, "Onion", "Vegetables", R.drawable.type_vegetable_onion),
    CategoryIconItem(105, "PeaPod", "Vegetables", R.drawable.type_vegetable_pea_pod),
    CategoryIconItem(106, "Potato", "Vegetables", R.drawable.type_vegetable_potato),

    // Finance
    CategoryIconItem(107, "Bank", "Finance", R.drawable.type_finance_bank),
    CategoryIconItem(108, "Bar Chart", "Finance", R.drawable.type_finance_bar_chart),
    CategoryIconItem(109, "Char Decreasing", "Finance", R.drawable.type_finance_chart_decreasing),
    CategoryIconItem(110, "Chart Increasing", "Finance", R.drawable.type_finance_chart_increasing),
    CategoryIconItem(111, "Chart Increasing with Yen", "Finance", R.drawable.type_finance_chart_increasing_with_yen),
    CategoryIconItem(112, "Classical Building", "Finance", R.drawable.type_finance_classical_building),
    CategoryIconItem(113, "Coin", "Finance", R.drawable.type_finance_coin),
    CategoryIconItem(114, "Currency exchange", "Finance", R.drawable.type_finance_currency_exchange),
    CategoryIconItem(115, "Dollar Banknote", "Finance", R.drawable.type_finance_dollar_banknote),
    CategoryIconItem(116, "Euro Banknote", "Finance", R.drawable.type_finance_euro_banknote),
    CategoryIconItem(117, "Heavy Dollar Sign", "Finance", R.drawable.type_finance_heavy_dollar_sign),
    CategoryIconItem(118, "Money Wings", "Finance", R.drawable.type_finance_money_with_wings),
    CategoryIconItem(119, "Money Bag", "Finance", R.drawable.type_finance_money_bag),
    CategoryIconItem(120, "Pound Banknote", "Finance", R.drawable.type_finance_pound_banknote),
    CategoryIconItem(121, "Yen Banknote", "Finance", R.drawable.type_finance_yen_banknote),

    // Logos
    CategoryIconItem(122, "Apple Tv", "Logos", R.drawable.type_logo_apple_tv),
    CategoryIconItem(123, "Netflix", "Logos", R.drawable.type_logo_netflix),
    CategoryIconItem(124, "SoundCloud", "Logos", R.drawable.type_logo_soundcloud),
    CategoryIconItem(125, "Spotify", "Logos", R.drawable.type_logo_spotify),
    CategoryIconItem(126, "Swiggy", "Logos", R.drawable.type_logo_swiggy),
    CategoryIconItem(127, "Youtube", "Logos", R.drawable.type_logo_youtube),
    CategoryIconItem(128, "Zomato", "Logos", R.drawable.type_logo_zomato),

    // Groceries
    CategoryIconItem(129, "Baby Bottle", "Groceries", R.drawable.type_groceries_baby_bottle),
    CategoryIconItem(130, "Baguette Bread", "Groceries", R.drawable.type_groceries_baguette_bread),
    CategoryIconItem(131, "Basket", "Groceries", R.drawable.type_groceries_basket),
    CategoryIconItem(132, "Bread", "Groceries", R.drawable.type_groceries_bread),
    CategoryIconItem(133, "Butter", "Groceries", R.drawable.type_groceries_butter),
    CategoryIconItem(134, "Candle", "Groceries", R.drawable.type_groceries_candle),
    CategoryIconItem(135, "Cheese Wedge", "Groceries", R.drawable.type_groceries_cheese_wedge),
    CategoryIconItem(136, "Meat", "Groceries", R.drawable.type_groceries_cut_of_meat),
    CategoryIconItem(137, "Egg", "Groceries", R.drawable.type_groceries_egg),
    CategoryIconItem(138, "Fish", "Groceries", R.drawable.type_groceries_fish),
    CategoryIconItem(139, "FlatBread", "Groceries", R.drawable.type_groceries_flatbread),
    CategoryIconItem(140, "Milk", "Groceries", R.drawable.type_groceries_glass_of_milk),
    CategoryIconItem(141, "Lotion", "Groceries", R.drawable.type_groceries_lotion_bottle),
    CategoryIconItem(142, "Soap", "Groceries", R.drawable.type_groceries_soap),
    CategoryIconItem(143, "Sponge", "Groceries", R.drawable.type_groceries_sponge),
    CategoryIconItem(144, "Spoon", "Groceries", R.drawable.type_groceries_spoon),
    CategoryIconItem(145, "Toothbrush", "Groceries", R.drawable.type_groceries_toothbrush),

    //Health
    CategoryIconItem(146, "Bandage", "Health", R.drawable.type_health_adhesive_bandage),
    CategoryIconItem(147, "Ambulance", "Health", R.drawable.type_health_ambulance),
    CategoryIconItem(148, "Cigarette", "Health", R.drawable.type_health_cigarette),
    CategoryIconItem(149, "DNA", "Health", R.drawable.type_health_dna),
    CategoryIconItem(150, "Blood", "Health", R.drawable.type_health_drop_of_blood),
    CategoryIconItem(151, "Doctor", "Health", R.drawable.type_health_health_worker_light),
    CategoryIconItem(152, "Hospital", "Health", R.drawable.type_health_hospital),
    CategoryIconItem(153, "Mending Heart", "Health", R.drawable.type_health_mending_heart),
    CategoryIconItem(154, "Medicines", "Health", R.drawable.type_health_pill),
    CategoryIconItem(155, "Stethoscope", "Health", R.drawable.type_health_stethoscope),
    CategoryIconItem(156, "Syringe", "Health", R.drawable.type_health_syringe),
    CategoryIconItem(157, "Thermometer", "Health", R.drawable.type_health_thermometer),
    CategoryIconItem(158, "tooth", "Health", R.drawable.type_health_tooth),
    CategoryIconItem(159, "XRay", "Health", R.drawable.type_health_x_ray),

    // Shopping
    CategoryIconItem(160, "Backpack", "Shopping", R.drawable.type_shopping_backpack),
    CategoryIconItem(161, "Ballet Shoe", "Shopping", R.drawable.type_shopping_ballet_shoes),
    CategoryIconItem(162, "Bikini", "Shopping", R.drawable.type_shopping_bikini),
    CategoryIconItem(163, "Billed Cap", "Shopping", R.drawable.type_shopping_billed_cap),
    CategoryIconItem(164, "Briefcase", "Shopping", R.drawable.type_shopping_briefcase),
    CategoryIconItem(165, "Clutch Bag", "Shopping", R.drawable.type_shopping_clutch_bag),
    CategoryIconItem(166, "Coat", "Shopping", R.drawable.type_shopping_coat),
    CategoryIconItem(167, "Dress", "Shopping", R.drawable.type_shopping_dress),
    CategoryIconItem(168, "Gem Stone", "Shopping", R.drawable.type_shopping_gem_stone),
    CategoryIconItem(169, "Glasses", "Shopping", R.drawable.type_shopping_glasses),
    CategoryIconItem(170, "Gloves", "Shopping", R.drawable.type_shopping_gloves),
    CategoryIconItem(171, "Goggle", "Shopping", R.drawable.type_shopping_goggles),
    CategoryIconItem(172, "Handbag", "Shopping", R.drawable.type_shopping_handbag),
    CategoryIconItem(173, "HighHeel Shoe", "Shopping", R.drawable.type_shopping_high_heeled_shoe),
    CategoryIconItem(174, "Hiking Boot", "Shopping", R.drawable.type_shopping_hiking_boot),
    CategoryIconItem(175, "Jeans", "Shopping", R.drawable.type_shopping_jeans),
    CategoryIconItem(176, "Kimono", "Shopping", R.drawable.type_shopping_kimono),
    CategoryIconItem(177, "Lab Coat", "Shopping", R.drawable.type_shopping_lab_coat),
    CategoryIconItem(178, "Lipstick", "Shopping", R.drawable.type_shopping_lipstick),
    CategoryIconItem(179, "Mans Shoe", "Shopping", R.drawable.type_shopping_mans_shoe),
    CategoryIconItem(180, "Martial Arts Suit", "Shopping", R.drawable.type_shopping_martial_arts_uniform),
    CategoryIconItem(181, "Nail Polish", "Shopping", R.drawable.type_shopping_nail_polish),
    CategoryIconItem(182, "Necktie", "Shopping", R.drawable.type_shopping_necktie),
    CategoryIconItem(183, "OnePiece Swimming Suit", "Shopping", R.drawable.type_shopping_one_piece_swimsuit),
    CategoryIconItem(184, "Purse", "Shopping", R.drawable.type_shopping_purse),
    CategoryIconItem(185, "Sari", "Shopping", R.drawable.type_shopping_sari),
    CategoryIconItem(186, "Scarf", "Shopping", R.drawable.type_shopping_scarf),
    CategoryIconItem(187, "Shopping Bags", "Shopping", R.drawable.type_shopping_shopping_bags),
    CategoryIconItem(188, "Shopping cart", "Shopping", R.drawable.type_shopping_shopping_cart),
    CategoryIconItem(189, "Shorts", "Shopping", R.drawable.type_shopping_shorts),
    CategoryIconItem(190, "Socks", "Shopping", R.drawable.type_shopping_socks),
    CategoryIconItem(191, "Sunglasses", "Shopping", R.drawable.type_shopping_sunglasses),
    CategoryIconItem(192, "Top Hat", "Shopping", R.drawable.type_shopping_top_hat),
    CategoryIconItem(193, "T-shirt", "Shopping", R.drawable.type_shopping_t_shirt),

    // Sports
    CategoryIconItem(194, "FirstMedal", "Sports", R.drawable.type_sports_1st_place_medal),
    CategoryIconItem(195, "SecondMedal", "Sports", R.drawable.type_sports_2nd_place_medal),
    CategoryIconItem(196, "ThirdMedal", "Sports", R.drawable.type_sports_3rd_place_medal),
    CategoryIconItem(197, "American Football", "Sports", R.drawable.type_sports_american_football),
    CategoryIconItem(198, "Badminton", "Sports", R.drawable.type_sports_badminton),
    CategoryIconItem(199, "Basketball", "Sports", R.drawable.type_sports_basketball),
    CategoryIconItem(200, "Boomerang", "Sports", R.drawable.type_sports_boomerang),
    CategoryIconItem(201, "Bow And Arrow", "Sports", R.drawable.type_sports_bow_and_arrow),
    CategoryIconItem(202, "Bowling", "Sports", R.drawable.type_sports_bowling),
    CategoryIconItem(203, "Boxing Glove", "Sports", R.drawable.type_sports_boxing_glove),
    CategoryIconItem(204, "Bullseye", "Sports", R.drawable.type_sports_bullseye),
    CategoryIconItem(205, "Chequered Flag", "Sports", R.drawable.type_sports_chequered_flag),
    CategoryIconItem(206, "Chess Pawn", "Sports", R.drawable.type_sports_chess_pawn),
    CategoryIconItem(207, "Cricket Game", "Sports", R.drawable.type_sports_cricket_game),
    CategoryIconItem(208, "Diving Mask", "Sports", R.drawable.type_sports_diving_mask),
    CategoryIconItem(209, "Field Hockey", "Sports", R.drawable.type_sports_field_hockey),
    CategoryIconItem(210, "Fishing Pole", "Sports", R.drawable.type_sports_fishing_pole),
    CategoryIconItem(211, "Flag in Hole", "Sports", R.drawable.type_sports_flag_in_hole),
    CategoryIconItem(212, "Flexed Biceps", "Sports", R.drawable.type_sports_flexed_biceps_light),
    CategoryIconItem(213, "Game Die", "Sports", R.drawable.type_sports_game_die),
    CategoryIconItem(214, "Ice Hockey", "Sports", R.drawable.type_sports_ice_hockey),
    CategoryIconItem(215, "Ice Skate", "Sports", R.drawable.type_sports_ice_skate),
    CategoryIconItem(216, "Lacrosse", "Sports", R.drawable.type_sports_lacrosse),
    CategoryIconItem(217, "Man lifting Weights ", "Sports", R.drawable.type_sports_man_lifting_weights),
    CategoryIconItem(218, "Mechanical Arm", "Sports", R.drawable.type_sports_mechanical_arm),
    CategoryIconItem(219, "Military Medal", "Sports", R.drawable.type_sports_military_medal),
    CategoryIconItem(220, "Ping Pong", "Sports", R.drawable.type_sports_ping_pong),
    CategoryIconItem(221, "Pool 8 Ball", "Sports", R.drawable.type_sports_pool_8_ball),
    CategoryIconItem(222, "Rugby Football", "Sports", R.drawable.type_sports_rugby_football),
    CategoryIconItem(223, "Skateboard", "Sports", R.drawable.type_sports_skateboard),
    CategoryIconItem(224, "Snowboarder", "Sports", R.drawable.type_sports_snowboarder_light),
    CategoryIconItem(225, "Soccer Ball", "Sports", R.drawable.type_sports_soccer_ball),
    CategoryIconItem(226, "SoftBall", "Sports", R.drawable.type_sports_softball),
    CategoryIconItem(227, "SportsMedal", "Sports", R.drawable.type_sports_sports_medal),
    CategoryIconItem(228, "Tennis", "Sports", R.drawable.type_sports_tennis),
    CategoryIconItem(229, "Trophy", "Sports", R.drawable.type_sports_trophy),
    CategoryIconItem(230, "VolleyBall", "Sports", R.drawable.type_sports_volleyball),
    CategoryIconItem(231, "Woman Lifting Weights", "Sports", R.drawable.type_sports_woman_lifting_weights),

    // Musical Instruments
    CategoryIconItem(232, "Accordion", "Musical Instruments", R.drawable.type_musical_instrument_accordion),
    CategoryIconItem(233, "Banjo", "Musical Instruments", R.drawable.type_musical_instrument_banjo),
    CategoryIconItem(234, "Flute", "Musical Instruments", R.drawable.type_musical_instrument_flute),
    CategoryIconItem(235, "Guitar", "Musical Instruments", R.drawable.type_musical_instrument_guitar),
    CategoryIconItem(236, "Musical Keyboard", "Musical Instruments", R.drawable.type_musical_instrument_musical_keyboard),
    CategoryIconItem(237, "Saxophone", "Musical Instruments", R.drawable.type_musical_instrument_saxophone),
    CategoryIconItem(238, "Trumpet", "Musical Instruments", R.drawable.type_musical_instrument_trumpet),
    CategoryIconItem(239, "Violin", "Musical Instruments", R.drawable.type_musical_instrument_violin),

    // Stationary
    CategoryIconItem(240, "Artist Palette", "Stationary", R.drawable.type_stationary_artist_palette),
    CategoryIconItem(241, "Blue Book", "Stationary", R.drawable.type_stationary_blue_book),
    CategoryIconItem(242, "Bookmark", "Stationary", R.drawable.type_stationary_bookmark),
    CategoryIconItem(243, "Bookmark tabs", "Stationary", R.drawable.type_stationary_bookmark_tabs),
    CategoryIconItem(244, "Books", "Stationary", R.drawable.type_stationary_books),
    CategoryIconItem(245, "Card fileBox", "Stationary", R.drawable.type_stationary_card_file_box),
    CategoryIconItem(246, "Card Index", "Stationary", R.drawable.type_stationary_card_index),
    CategoryIconItem(247, "Clipboard", "Stationary", R.drawable.type_stationary_clipboard),
    CategoryIconItem(248, "Closed Book", "Stationary", R.drawable.type_stationary_closed_book),
    CategoryIconItem(249, "Fountain pen", "Stationary", R.drawable.type_stationary_fountain_pen),
    CategoryIconItem(250, "Green Book", "Stationary", R.drawable.type_stationary_green_book),
    CategoryIconItem(251, "Label", "Stationary", R.drawable.type_stationary_label),
    CategoryIconItem(252, "Ledger", "Stationary", R.drawable.type_stationary_ledger),
    CategoryIconItem(253, "Link", "Stationary", R.drawable.type_stationary_link),
    CategoryIconItem(254, "Linked PaperClip", "Stationary", R.drawable.type_stationary_linked_paperclips),
    CategoryIconItem(255, "Memo", "Stationary", R.drawable.type_stationary_memo),
    CategoryIconItem(256, "Musical Score", "Stationary", R.drawable.type_stationary_musical_score),
    CategoryIconItem(257, "Newspaper", "Stationary", R.drawable.type_stationary_newspaper),
    CategoryIconItem(258, "Notebook", "Stationary", R.drawable.type_stationary_notebook),
    CategoryIconItem(259, "Notebook Decorative", "Stationary", R.drawable.type_stationary_notebook_with_decorative_cover),
    CategoryIconItem(260, "Open Book", "Stationary", R.drawable.type_stationary_open_book),
    CategoryIconItem(261, "Orange Book", "Stationary", R.drawable.type_stationary_orange_book),
    CategoryIconItem(262, "Page", "Stationary", R.drawable.type_stationary_page_facing_up),
    CategoryIconItem(263, "Page Curl", "Stationary", R.drawable.type_stationary_page_with_curl),
    CategoryIconItem(264, "Paint Brush", "Stationary", R.drawable.type_stationary_paintbrush),
    CategoryIconItem(265, "PaperClip", "Stationary", R.drawable.type_stationary_paperclip),
    CategoryIconItem(266, "Pen", "Stationary", R.drawable.type_stationary_pen),
    CategoryIconItem(267, "Pencil", "Stationary", R.drawable.type_stationary_pencil),
    CategoryIconItem(268, "Pushpin", "Stationary", R.drawable.type_stationary_pushpin),
    CategoryIconItem(269, "Reminder Ribbon", "Stationary", R.drawable.type_stationary_reminder_ribbon),
    CategoryIconItem(270, "Ribbon", "Stationary", R.drawable.type_stationary_ribbon),
    CategoryIconItem(271, "Scroll", "Stationary", R.drawable.type_stationary_scroll),
    CategoryIconItem(272, "Spiral Notepad", "Stationary", R.drawable.type_stationary_spiral_notepad),
    CategoryIconItem(273, "Straight Ruler", "Stationary", R.drawable.type_stationary_straight_ruler),
    CategoryIconItem(274, "Wrapped Gift", "Stationary", R.drawable.type_stationary_wrapped_gift),
    CategoryIconItem(275, "Writing Hand", "Stationary", R.drawable.type_stationary_writing_hand_light),

    )

