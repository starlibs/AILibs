CREATE TABLE IF NOT EXISTS `test` (
  `A` int(11) NOT NULL,
  `B` varchar(20) COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Daten für Tabelle `test`
--

INSERT INTO `test` (`A`, `B`) VALUES
(1, 'b'),
(2, 'c');
