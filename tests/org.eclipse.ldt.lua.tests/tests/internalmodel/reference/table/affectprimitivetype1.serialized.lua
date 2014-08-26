do local _={
		unknownglobalvars={

		}
		--[[table: 0x94d7050]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="table",
								fields={
									fieldname={
										type={
											typename="number",
											tag="primitivetyperef"
										}
										--[[table: 0x93b2d48]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=32,
											max=40
										}
										--[[table: 0x93bf318]],
										occurrences={

										}
										--[[table: 0x93bf2f0]],
										tag="item"
									}
									--[[table: 0x93b2d70]]
								}
								--[[table: 0x950b2e8]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x93b2c98]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x950b2c0]],
							tag="inlinetyperef"
						}
						--[[table: 0x93b2d20]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x950b298]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9557cd0]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x97ecee0]],
							[2]={
								sourcerange={
									min=22,
									max=30
								}
								--[[table: 0x97e19c8]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x98566f0]]
						}
						--[[table: 0x9639320]],
						tag="item"
					}
					--[[table: 0x9639290]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x975b028]]
				}
				--[[table: 0x95824b0]]
			}
			--[[table: 0x958f548]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x958f570]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=22,
						max=40
					}
					--[[table: 0x97e1a18]],
					right="fieldname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x97e19f0]]
			}
			--[[table: 0x958f520]],
			tag="MBlock"
		}
		--[[table: 0x94d7078]],
		tag="MInternalContent"
	}
	--[[table: 0x97097f0]];
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	return _;
end